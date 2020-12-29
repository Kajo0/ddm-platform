package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.LabeledObservation;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.MEBClustering;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.MEBModel;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.SVMModel;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.Utils;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.WekaSVMClassification;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.WekaUtils;
import pl.edu.pw.ddm.platform.interfaces.algorithm.AlgorithmConfig;
import pl.edu.pw.ddm.platform.interfaces.algorithm.DdmPipeline;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.CentralDdmPipeline;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.GlobalUpdater;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.LocalProcessor;
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import weka.core.neighboursearch.LinearNNSearch;

public class DMeb2 implements LocalProcessor<LocalRepresentativesModel>,
        GlobalUpdater<LocalRepresentativesModel, GlobalClassifier>,
        AlgorithmConfig {

    @Override
    public LocalRepresentativesModel processLocal(DataProvider dataProvider, ParamProvider paramProvider) {
        printParams(paramProvider);

        List<LabeledObservation> labeledObservations = toLabeledObservation(dataProvider.training());
        String kernel = paramProvider.provide("kernel");
        SVMModel svmModel = new WekaSVMClassification(kernel).train(labeledObservations);

        // FIXME unused partitionId?
        int partitionId = 0;
        Double mebClusters = paramProvider.provideNumeric("meb_clusters", 32d);
        if (mebClusters <= 0) {
            mebClusters = Math.max(2, Math.ceil(Math.pow(Math.log(dataProvider.training().size()), 2)));
            System.out.println("  [[FUTURE LOG]] MEB clusters calculated=" + mebClusters);
        }
        String initMethod = paramProvider.provide("init_kmeans_method", "k-means++");
        MEBModel mebModel = new MEBClustering(mebClusters.intValue(), initMethod).perform(labeledObservations, partitionId);

        List<LabeledObservation> representativeList = mebModel.getClusterList().stream()
                .flatMap(cluster -> {
                    if (useBorderLeaveFeature(paramProvider)) {
                        return cluster.leaveBorder(paramProvider.distanceFunction()).stream();
                    } else if (Utils.moreThanOneClass(cluster.getClusterElementList())) {
                        return cluster.getClusterElementList().stream();
                    } else {
                        return Stream.of(cluster.squashToCentroid());
                    }
                })
                .collect(Collectors.toList());

        System.out.println("  [[FUTURE LOG]] processLocal: representativeList=" + representativeList.size()
                + ", labeledObservations=" + labeledObservations.size());

        if (useLocalClassifier(paramProvider)) {
            LabeledObservation dummyObservation = LocalRepresentativesModel.dummyObservation();
            return new LocalRepresentativesModel(svmModel, Collections.singletonList(dummyObservation));
        } else {
            return new LocalRepresentativesModel(svmModel, representativeList);
        }
    }

    private List<LabeledObservation> toLabeledObservation(Collection<Data> training) {
        return training.stream()
                .map(d -> new LabeledObservation(Integer.parseInt(d.getId()), d.getNumericAttributes(), (int) Double.parseDouble(d.getLabel()))) // FIXME int label/index
                .collect(Collectors.toList());
    }

    @Override
    public GlobalClassifier updateGlobal(Collection<LocalRepresentativesModel> localModels, ParamProvider paramProvider) {
        if (useLocalClassifier(paramProvider)) {
            return new GlobalClassifier(null, localModels.toArray(new LocalRepresentativesModel[]{}), new HashMap<>());
        }

        List<LabeledObservation> trainingSet = localModels.stream()
                .flatMap(localModel -> localModel.getRepresentativeList().stream())
                .collect(toList());
        String kernel = paramProvider.provide("kernel");
        SVMModel svmModel = new WekaSVMClassification(kernel).train(trainingSet);

        LocalRepresentativesModel[] localModelArray = localModels.toArray(new LocalRepresentativesModel[0]);
        Map<Integer, List<LabeledObservation>> classToLocalModel = new HashMap<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < localModelArray.length; i++) {
            for (LabeledObservation observation : localModelArray[i].getRepresentativeList()) {
                List<LabeledObservation> localModel = classToLocalModel.get(observation.getTarget());
                if (localModel == null) {
                    localModel = new ArrayList<>();
                }
                localModel.add(new LabeledObservation(-1, observation.getFeatures(), i));
                classToLocalModel.put(observation.getTarget(), localModel);
            }
            labels.add("" + i);
        }
        Map<Integer, LinearNNSearch> knnModelMap = new HashMap<>();
        for (Map.Entry<Integer, List<LabeledObservation>> entry : classToLocalModel.entrySet()) {
            knnModelMap.put(entry.getKey(), new LinearNNSearch(WekaUtils.convertToInstances(entry.getValue(), labels)));
        }

        Arrays.stream(localModelArray)
                .forEach(LocalRepresentativesModel::clearRepresentativesButDummy);

        System.out.println("  [[FUTURE LOG]] updateGlobal: svs=" + svmModel.getSVs().size()
                + ", localModelArray=" + localModelArray.length + ", knnModelMap=" + knnModelMap.size());
        return new GlobalClassifier(svmModel, localModelArray, knnModelMap);
    }

    @Override
    public DdmPipeline pipeline() {
        return CentralDdmPipeline.builder()
                .local(DMeb2.class)
                .lastGlobal(DMeb2.class);
    }

    static boolean useLocalClassifier(ParamProvider paramProvider) {
        return Boolean.TRUE.toString().equals(paramProvider.provide("use_local_classifier", Boolean.FALSE.toString()));
    }

    static boolean useBorderLeaveFeature(ParamProvider paramProvider) {
        return Boolean.TRUE.toString().equals(paramProvider.provide("border_leave", Boolean.FALSE.toString()));
    }

    private void printParams(ParamProvider paramProvider) {
        System.out.println("---------------------------------");
        System.out.println("-     PARAMS                    -");
        System.out.println("---------------------------------");
        paramProvider.allParams()
                .forEach((k, v) -> System.out.println("  " + k + "=" + v));
        System.out.println("---------------------------------");
    }

}
