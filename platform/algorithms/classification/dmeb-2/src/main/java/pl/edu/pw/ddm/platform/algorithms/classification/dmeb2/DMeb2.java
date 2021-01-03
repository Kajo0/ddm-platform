package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import de.lmu.ifi.dbs.elki.data.synthetic.bymodel.GeneratorSingleCluster;
import de.lmu.ifi.dbs.elki.math.statistics.distribution.NormalDistribution;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.LabeledObservation;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.LabelledWrapper;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.MEBCluster;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.MEBClustering;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.MEBModel;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.SVMModel;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.WekaSVMClassification;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.WekaUtils;
import pl.edu.pw.ddm.platform.distfunc.EuclideanDistance;
import pl.edu.pw.ddm.platform.interfaces.algorithm.AlgorithmConfig;
import pl.edu.pw.ddm.platform.interfaces.algorithm.DdmPipeline;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.CentralDdmPipeline;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.GlobalUpdater;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.LocalProcessor;
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;
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
        WekaSVMClassification wekaClassifier = new WekaSVMClassification(kernel, seed(paramProvider));
        SVMModel svmModel = wekaClassifier.train(labeledObservations);
        System.out.println("  [[FUTURE LOG]] SVM svs found=" + svmModel.getSVs().size());

        // FIXME unused partitionId?
        int partitionId = 0;
        Double mebClusters = paramProvider.provideNumeric("meb_clusters", 32d);
        if (mebClusters <= 0) {
            mebClusters = Math.max(2, Math.ceil(Math.pow(Math.log(dataProvider.training().size()), 2)));
            System.out.println("  [[FUTURE LOG]] MEB clusters calculated=" + mebClusters);
        }

        boolean debug = Boolean.TRUE.toString().equals(paramProvider.provide("debug", "false"));
        String initMethod = paramProvider.provide("init_kmeans_method", "k-means++");
        DistanceFunction distanceFunction = Optional.ofNullable(paramProvider.distanceFunction())
                .orElseGet(EuclideanDistance::new);
        MEBModel mebModel = new MEBClustering(mebClusters.intValue(), initMethod, distanceFunction, debug)
                .perform(labeledObservations, partitionId);

        Set<LabeledObservation> representativeList = new HashSet<>(svmModel.getSVs());

        String localForSvs = paramProvider.provide("local_method_for_svs_clusters", "all_with_svs");
        System.out.println("  [[FUTURE LOG]] local_method_for_svs_clusters: " + localForSvs);
        switch (localForSvs) {
            case "leave_border":
                mebModel.getClusterList()
                        .stream()
                        .filter(cluster -> cluster.containsAny(svmModel.getSVs()))
                        .map(MEBCluster::leaveBorder)
                        .forEach(representativeList::addAll);
                break;
            case "close_to":
                double closeToPercent = paramProvider.provideNumeric("close_to_percent", 0.2);
                mebModel.getClusterList()
                        .stream()
                        .filter(cluster -> cluster.containsAny(svmModel.getSVs()))
                        .map(cluster -> cluster.leaveCloseToSvs(closeToPercent, svmModel.getSVs()))
                        .forEach(representativeList::addAll);
                break;
            case "all_with":
                mebModel.getClusterList()
                        .stream()
                        .filter(cluster -> cluster.containsAny(svmModel.getSVs()))
                        .map(MEBCluster::getClusterElementList)
                        .forEach(representativeList::addAll);
                break;
            case "all_when_multi":
            default:
                mebModel.getClusterList()
                        .stream()
                        .filter(cluster -> cluster.isMultiClass() || cluster.containsAny(svmModel.getSVs()))
                        .map(MEBCluster::getClusterElementList)
                        .forEach(representativeList::addAll);
                break;
        }

        String localForNonMulti = paramProvider.provide("local_method_for_non_multiclass_clusters", "squash_to_centroid");
        System.out.println("  [[FUTURE LOG]] local_method_for_non_multiclass_clusters: " + localForNonMulti);
        switch (localForNonMulti) {
            case "metrics_collect":
                mebModel.singleClassClusters()
                        .stream()
                        .filter(cluster -> !cluster.containsAny(svmModel.getSVs()))
                        .peek(MEBCluster::calculateMetrics)
                        .peek(MEBCluster::squashToCentroid)
                        .map(LabelledWrapper::ofPrecalculated)
                        .forEach(representativeList::add);
                break;
            case "random":
                double randomPercent = paramProvider.provideNumeric("random_percent", 0.1);
                mebModel.singleClassClusters()
                        .stream()
                        .filter(cluster -> !cluster.containsAny(svmModel.getSVs()))
                        .map(cluster -> cluster.random(randomPercent, seed(paramProvider)))
                        .forEach(representativeList::addAll);
                break;
            case "squash_to_centroid":
            default:
                mebModel.singleClassClusters()
                        .stream()
                        .filter(cluster -> !cluster.containsAny(svmModel.getSVs()))
                        .map(MEBCluster::squashToCentroid)
                        .forEach(representativeList::add);
                break;
        }

        System.out.println("  [[FUTURE LOG]] processLocal: svs=" + svmModel.getSVs().size() + ", representativeList="
                + representativeList.size() + ", labeledObservations=" + labeledObservations.size());

        if (useLocalClassifier(paramProvider)) {
            LabeledObservation dummyObservation = LocalRepresentativesModel.dummyObservation();
            return new LocalRepresentativesModel(svmModel, Collections.singleton(dummyObservation));
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
        System.out.println("  [[FUTURE LOG]] updateGlobal: local trainSet=" + trainingSet.size());

        double percent = paramProvider.provideNumeric("global_expand_percent", 0.2);
        trainingSet.addAll(expandModels(localModels, percent, seed(paramProvider)));
        System.out.println("  [[FUTURE LOG]] updateGlobal: expanded trainSet=" + trainingSet.size());

        String kernel = paramProvider.provide("kernel");
        SVMModel svmModel = new WekaSVMClassification(kernel, seed(paramProvider)).train(trainingSet);

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
                + ", localModelArray=" + localModelArray.length + ", knnModelClassMap=" + knnModelMap.size());
        svmModel.getSVs().clear(); // as global classifier does not need them anymore cached in model
        return new GlobalClassifier(svmModel, localModelArray, knnModelMap);
    }

    private List<LabeledObservation> expandModels(Collection<LocalRepresentativesModel> localModels, double percent, Long seed) {
        Random rand = Optional.ofNullable(seed)
                .map(Random::new)
                .orElseGet(Random::new);

        return localModels.stream()
                .map(LocalRepresentativesModel::getRepresentativeList)
                .flatMap(Collection::stream)
                .filter(o -> o instanceof LabelledWrapper)
                .map(o -> (LabelledWrapper) o)
                .flatMap(lw -> {
                    List<LabeledObservation> generated = new ArrayList<>();
                    generated.add(lw);
                    GeneratorSingleCluster gsc = new GeneratorSingleCluster("dummy", lw.getElements(), 1, rand);

                    for (int i = 0; i < lw.getFeatures().length; ++i) {
                        gsc.addGenerator(new NormalDistribution(lw.getMean()[i], lw.getStddev()[i], rand));
                    }
                    for (double[] data : gsc.generate((int) Math.max(1, lw.getElements() * percent))) {
                        generated.add(new LabeledObservation(-1, data, lw.getTarget()));
                    }

                    return generated.stream();
                })
                .collect(Collectors.toList());
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

    static Long seed(ParamProvider paramProvider) {
        Double seed = paramProvider.provideNumeric("seed");
        if (seed != null) {
            return seed.longValue();
        } else {
            return null;
        }
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
