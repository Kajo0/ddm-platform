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
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.DummySVMModel;
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
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.GlobalProcessor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.GlobalUpdater;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.LocalProcessor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.LocalRepeater;
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import weka.core.neighboursearch.LinearNNSearch;

public class DMeb2 implements LocalProcessor<LocalMinMaxModel>,
        GlobalProcessor<LocalMinMaxModel, GlobalMinMaxModel>,
        LocalRepeater<LocalMinMaxModel, LocalRepresentativesModel, GlobalMinMaxModel>,
        GlobalUpdater<LocalRepresentativesModel, GlobalClassifier>,
        AlgorithmConfig {

    @Override
    public LocalMinMaxModel processLocal(DataProvider dataProvider, ParamProvider paramProvider) {
        printParams(paramProvider);

        if (!performGlobalNormalization(paramProvider)) {
            return new LocalMinMaxModel(null);
        }

        System.out.println("  [[FUTURE LOG]] preProcessLocal: Searching for min max within " + dataProvider.training().size());
        List<Data> minMax = new MinMaxExtractor(dataProvider.training()).search();
        System.out.println("  [[FUTURE LOG]] preProcessLocal: Found minMaxes=" + minMax);

        return new LocalMinMaxModel(new HashSet<>(minMax));
    }

    @Override
    public GlobalMinMaxModel processGlobal(Collection<LocalMinMaxModel> collection, ParamProvider paramProvider) {
        if (!performGlobalNormalization(paramProvider)) {
            return new GlobalMinMaxModel(null, null);
        }

        Set<Data> data = collection.stream()
                .map(LocalMinMaxModel::getObservations)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        System.out.println("  [[FUTURE LOG]] preProcessGlobal: Searching for min max within " + data.size());

        List<Data> minMax = new MinMaxExtractor(data).search();
        System.out.println("  [[FUTURE LOG]] preProcessGlobal: Found minMaxes=" + minMax);

        return new GlobalMinMaxModel(minMax.get(0), minMax.get(1));
    }

    @Override
    public LocalRepresentativesModel repeatLocal(GlobalMinMaxModel globalMinMaxModel, LocalMinMaxModel localMinMaxModel,
            DataProvider dataProvider, ParamProvider paramProvider) {
        List<LabeledObservation> labeledObservations = toLabeledObservation(dataProvider.training());
        int trainingSize = dataProvider.training().size();
        Set<LabeledObservation> representativeList = new HashSet<>();

        String localForSvs = paramProvider.provide("local_method_for_svs_clusters", "all_with_svs");
        System.out.println("  [[FUTURE LOG]] local_method_for_svs_clusters: " + localForSvs);

        SVMModel svmModel = null;
        int mebClusters = 0;

        if (!"just_random".equals(localForSvs) || !firstLevelOnly(paramProvider)) {
            String kernel = paramProvider.provide("kernel");
            WekaSVMClassification wekaClassifier = new WekaSVMClassification(kernel, seed(paramProvider));
            if (performGlobalNormalization(paramProvider)) {
                wekaClassifier.setMinAttrValues(globalMinMaxModel.getMinValues().getNumericAttributes());
                wekaClassifier.setMaxAttrValues(globalMinMaxModel.getMaxValues().getNumericAttributes());
            }
            svmModel = wekaClassifier.train(labeledObservations);
            System.out.println("  [[FUTURE LOG]] SVM svs found=" + svmModel.getSVs().size());

            if (!"just_random".equals(localForSvs)) {
                representativeList.addAll(svmModel.getSVs());
            }
        }

        if ("just_random".equals(localForSvs)) {
            System.out.println("  [[FUTURE LOG]] Just random observations from entire data");
            Random rand = Optional.ofNullable(seed(paramProvider))
                    .map(Random::new)
                    .orElseGet(Random::new);
            // TODO FIXME optimize but here we've got non returned new Random().ints() 'stream'
            Collections.shuffle(labeledObservations, rand);

            double randomPercent = paramProvider.provideNumeric("random_percent", 0.2);
            long amount = (long) (trainingSize * randomPercent);
            labeledObservations.stream()
                    .limit(Math.max(1, amount))
                    .forEach(representativeList::add);
            System.out.println("  [[FUTURE LOG]] Randomly chosen " + amount + " of " + trainingSize + " elements");

            if (firstLevelOnly(paramProvider)) {
                svmModel = new DummySVMModel(Integer.MIN_VALUE);
            }
        } else if ("svs_only".equals(localForSvs)) {
            System.out.println("  [[FUTURE LOG]] Svs only from entire data");
        } else {
            // FIXME unused partitionId?
            int partitionId = 0;
            mebClusters = paramProvider.provideNumeric("meb_clusters", 32d).intValue();
            int labelMultiplier = 1;
            if (mebClusters == -2) {
                labelMultiplier = (int) dataProvider.training()
                        .stream()
                        .map(Data::getLabel)
                        .distinct()
                        .count();
                System.out.println("  [[FUTURE LOG]] Found " + labelMultiplier + " in " + trainingSize + " data samples");
            }
            if (mebClusters <= 0) {
                mebClusters = (int) Math.max(2, Math.ceil(Math.pow(Math.log(trainingSize), 2)));
                mebClusters *= labelMultiplier;
                mebClusters = (int) Math.min(mebClusters, Math.ceil((float) trainingSize / 2));
                System.out.println("  [[FUTURE LOG]] MEB clusters calculated=" + mebClusters);
            }

            String initMethod = paramProvider.provide("init_kmeans_method", "k-means++");
            DistanceFunction distanceFunction = Optional.ofNullable(paramProvider.distanceFunction())
                    .orElseGet(EuclideanDistance::new);
            MEBModel mebModel = new MEBClustering(mebClusters, initMethod, distanceFunction, debug(paramProvider))
                    .perform(labeledObservations, partitionId);
            mebModel.markSupportClusters(svmModel.getSVs());

            switch (localForSvs) {
                case "leave_border":
                    mebModel.getClusterList()
                            .stream()
                            .filter(cluster -> cluster.isMultiClass() || cluster.isSupportCluster())
                            .map(MEBCluster::leaveBorder)
                            .forEach(representativeList::addAll);
                    break;
                case "close_to":
                    double closeToPercent = paramProvider.provideNumeric("close_to_percent", 0.2);
                    double closeRandomPercent = paramProvider.provideNumeric("random_percent", 0.2);
                    SVMModel finalSvmModel = svmModel;
                    mebModel.getClusterList()
                            .stream()
                            .filter(MEBCluster::isSupportCluster)
                            .map(cluster -> cluster.leaveCloseToSvs(closeToPercent, finalSvmModel.getSVs()))
                            .forEach(representativeList::addAll);
                    mebModel.multiClassClusters()
                            .stream()
                            .filter(cluster -> !cluster.isSupportCluster())
                            .map(cluster -> cluster.random(closeRandomPercent, seed(paramProvider)))
                            .forEach(representativeList::addAll);
                    break;
                case "random":
                    double randomPercent = paramProvider.provideNumeric("random_percent", 0.2);
                    mebModel.getClusterList()
                            .stream()
                            .filter(cluster -> cluster.isMultiClass() || cluster.isSupportCluster())
                            .map(cluster -> cluster.random(randomPercent, seed(paramProvider)))
                            .forEach(representativeList::addAll);
                    break;
                case "all_with":
                    mebModel.getClusterList()
                            .stream()
                            .filter(MEBCluster::isSupportCluster)
                            .map(MEBCluster::getClusterElementList)
                            .forEach(representativeList::addAll);
                    break;
                case "all_when_multi":
                default:
                    mebModel.multiClassClusters()
                            .stream()
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
                            .filter(cluster -> !cluster.isSupportCluster())
                            .peek(MEBCluster::calculateMetrics)
                            .peek(MEBCluster::squashToCentroid)
                            .map(LabelledWrapper::ofPrecalculated)
                            .forEach(representativeList::add);
                    break;
                case "random":
                    double randomPercent = paramProvider.provideNumeric("random_percent", 0.1);
                    mebModel.singleClassClusters()
                            .stream()
                            .filter(cluster -> !cluster.isSupportCluster())
                            .map(cluster -> cluster.random(randomPercent, seed(paramProvider)))
                            .forEach(representativeList::addAll);
                    break;
                case "leave_border":
                    mebModel.singleClassClusters()
                            .stream()
                            .filter(cluster -> !cluster.isSupportCluster())
                            .map(MEBCluster::leaveBorder)
                            .forEach(representativeList::addAll);
                    break;
                case "squash_to_median":
                    mebModel.singleClassClusters()
                            .stream()
                            .filter(cluster -> !cluster.isSupportCluster())
                            .map(MEBCluster::squashToMedian)
                            .forEach(representativeList::add);
                    break;
                case "squash_to_centroid":
                default:
                    mebModel.singleClassClusters()
                            .stream()
                            .filter(cluster -> !cluster.isSupportCluster())
                            .map(MEBCluster::squashToCentroid)
                            .forEach(representativeList::add);
                    break;
            }
            System.out.println("  [[FUTURE LOG]] processLocal: svs=" + svmModel.getSVs().size() + ", representativeList="
                    + representativeList.size() + ", labeledObservations=" + labeledObservations.size());
        }

        if (useLocalClassifier(paramProvider)) {
            LabeledObservation dummyObservation = LocalRepresentativesModel.dummyObservation();
            return new LocalRepresentativesModel(svmModel, Collections.singleton(dummyObservation), trainingSize, mebClusters);
        } else {
            return new LocalRepresentativesModel(svmModel, representativeList, trainingSize, mebClusters);
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

        trainingSet.addAll(expandModels(localModels, paramProvider));
        System.out.println("  [[FUTURE LOG]] updateGlobal: expanded trainSet=" + trainingSet.size());

        String kernel = paramProvider.provide("kernel");
        WekaSVMClassification wekaClassifier = new WekaSVMClassification(kernel, seed(paramProvider));
        SVMModel svmModel = wekaClassifier.train(trainingSet);

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

    private List<LabeledObservation> expandModels(Collection<LocalRepresentativesModel> localModels, ParamProvider paramProvider) {
        String localForNonMulti = paramProvider.provide("local_method_for_non_multiclass_clusters", "squash_to_centroid");
        if (!"metrics_collect".equals(localForNonMulti)) {
            return Collections.emptyList();
        }

        double percent = paramProvider.provideNumeric("global_expand_percent", 0.2);
        boolean debug = debug(paramProvider);
        Random rand = Optional.ofNullable(seed(paramProvider))
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

                    double percentf = percent;
                    if (percentf < 0) {
                        percentf = lw.getClusterDensityPercent();
                        if (debug) {
                            System.out.println("  [[FUTURE LOG]] Expand percent precalculated into " + percentf + " value");
                        }
                    }

                    int limit = (int) Math.max(1, Math.ceil(lw.getElements() * percentf));
                    for (double[] data : gsc.generate(limit)) {
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
                .global(DMeb2.class)
                .repeatLocal(DMeb2.class)
                .lastGlobal(DMeb2.class);
    }

    static boolean useLocalClassifier(ParamProvider paramProvider) {
        return Boolean.TRUE.toString().equals(paramProvider.provide("use_local_classifier", Boolean.FALSE.toString()));
    }

    static boolean firstLevelOnly(ParamProvider paramProvider) {
        return Boolean.TRUE.toString().equals(paramProvider.provide("use_first_level_only", "false"));
    }

    static boolean performGlobalNormalization(ParamProvider paramProvider) {
        return Boolean.TRUE.toString().equals(paramProvider.provide("global_normalization", Boolean.FALSE.toString()));
    }

    static boolean debug(ParamProvider paramProvider) {
        return Boolean.TRUE.toString().equals(paramProvider.provide("debug", "false"));
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
