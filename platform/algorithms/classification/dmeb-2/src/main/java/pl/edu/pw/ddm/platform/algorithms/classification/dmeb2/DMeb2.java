package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.LabeledObservation;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.MEBClustering;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.MEBModel;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.SVMModel;
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

public class DMeb2 implements LocalProcessor<ThirdMethodLocalSVMWithRepresentatives>,
        GlobalUpdater<ThirdMethodLocalSVMWithRepresentatives, ThirdMethodGlobalClassificationModel>,
        AlgorithmConfig {

    @Override
    public ThirdMethodLocalSVMWithRepresentatives processLocal(DataProvider dataProvider, ParamProvider paramProvider) {
        List<LabeledObservation> labeledObservations = toLabeledObservation(dataProvider.training());
        String kernel = paramProvider.provide("kernel");
        SVMModel svmModel = new WekaSVMClassification(kernel).train(labeledObservations);

        // FIXME unused partitionId?
        int partitionId = 0;
        Double mebClusters = paramProvider.provideNumeric("meb_clusters", 32d);
        MEBModel mebModel = new MEBClustering(mebClusters.intValue()).perform(labeledObservations, partitionId);

        List<LabeledObservation> representativeList = mebModel.getClusterList().stream()
                .flatMap(cluster -> Stream.of(new LabeledObservation(-1,
                        cluster.getCentroid().getFeatures(),
                        cluster.getClusterElementList().get(0).getTarget())))
                .collect(toList());
        return new ThirdMethodLocalSVMWithRepresentatives(svmModel, representativeList);
    }

    private List<LabeledObservation> toLabeledObservation(Collection<Data> training) {
        return training.stream()
                .map(d -> new LabeledObservation(Integer.parseInt(d.getId()), d.getNumericAttributes(), Integer.parseInt(d.getLabel()))) // FIXME int label/index
                .collect(Collectors.toList());
    }

    @Override
    public ThirdMethodGlobalClassificationModel updateGlobal(Collection<ThirdMethodLocalSVMWithRepresentatives> localModels, ParamProvider paramProvider) {
        List<LabeledObservation> trainingSet = localModels.stream()
                .flatMap(localModel -> localModel.getRepresentativeList().stream())
                .collect(toList());
        String kernel = paramProvider.provide("kernel");
        SVMModel svmModel = new WekaSVMClassification(kernel).train(trainingSet);

        ThirdMethodLocalSVMWithRepresentatives[] localModelArray = localModels.toArray(new ThirdMethodLocalSVMWithRepresentatives[0]);
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

        return new ThirdMethodGlobalClassificationModel(svmModel, localModelArray, knnModelMap);
    }

    @Override
    public DdmPipeline pipeline() {
        return CentralDdmPipeline.builder()
                .local(DMeb2.class)
                .lastGlobal(DMeb2.class);
    }

}