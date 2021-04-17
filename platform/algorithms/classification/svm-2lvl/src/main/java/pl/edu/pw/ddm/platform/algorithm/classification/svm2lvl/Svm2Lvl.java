package pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import de.lmu.ifi.dbs.elki.data.synthetic.bymodel.GeneratorSingleCluster;
import de.lmu.ifi.dbs.elki.math.statistics.distribution.NormalDistribution;
import pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.filterers.ConvexHullLabeledFilter;
import pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.utils.Cluster;
import pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.utils.LabeledObservation;
import pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.utils.LabelledWrapper;
import pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.utils.PartitioningClustering;
import pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.utils.SVMModel;
import pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.utils.WekaSVMClassification;
import pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.utils.WekaUtils;
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

public class Svm2Lvl implements LocalProcessor<LocalMinMaxModel>,
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
        List<LabeledObservation> labeledObservationsFiltered = toLabeledObservation(dataProvider.training(), paramProvider);
        int trainingSize = dataProvider.training().size();
        Set<LabeledObservation> representatives = new HashSet<>();

        String localForSvs = paramProvider.provide("local_method_name", "centroids");
        System.out.println("  [[FUTURE LOG]] local_method_name: " + localForSvs);

        SVMModel svmModel = null;
        if (firstGlobalLevelOnly(paramProvider)) {
            System.out.println("  [[FUTURE LOG]] Not building local SVM classifier! due to use_first_global_level_only param");
        } else {
            String kernel = paramProvider.provide("kernel");
            WekaSVMClassification wekaClassifier = new WekaSVMClassification(kernel, seed(paramProvider));
            if (performGlobalNormalization(paramProvider)) {
                wekaClassifier.setMinAttrValues(globalMinMaxModel.getMinValues()
                        .getNumericAttributes());
                wekaClassifier.setMaxAttrValues(globalMinMaxModel.getMaxValues()
                        .getNumericAttributes());
            }
            svmModel = wekaClassifier.train(labeledObservationsFiltered);

            if (localForSvs.contains("with_svs")) {
                System.out.println("  [[FUTURE LOG]] SVM svs found=" + svmModel.getSVs().size());
                representatives.addAll(svmModel.getSVs());
            }
        }

        int kClusters = 0;
        DistanceFunction distanceFunction = Optional.ofNullable(paramProvider.distanceFunction())
                .orElseGet(EuclideanDistance::new);
        double randomPercent = paramProvider.provideNumeric("random_percent", -0.1);

        switch (localForSvs) {
            case "just_random":
                Cluster allCluster = new Cluster(distanceFunction, debug(paramProvider));
                allCluster.setElements(labeledObservationsFiltered);
                representatives.addAll(allCluster.random(randomPercent, seed(paramProvider)));
                break;

            case "only_with_svs":
                break;

            case "centroids_per_class_with_metrics_add_random":
            case "centroids_per_class_with_metrics":
            case "centroids_per_class_add_random":
            case "centroids_per_class": {
                kClusters = kGroupsParam(dataProvider, paramProvider, trainingSize);
                int finalKClusters = kClusters;
                String initMethod = paramProvider.provide("init_kmeans_method", "k-means++");
                List<Cluster> clusters = labeledObservationsFiltered.stream()
                        .collect(Collectors.groupingBy(LabeledObservation::getTarget))
                        .values()
                        .stream()
                        .map(r -> new PartitioningClustering(finalKClusters, initMethod, distanceFunction, debug(paramProvider)))
                        .map(pc -> pc.perform(labeledObservationsFiltered))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
                if ("centroids_per_class_with_metrics".equals(localForSvs)) {
                    clusters.stream()
                            .peek(Cluster::calculateMetrics)
                            .map(LabelledWrapper::ofPrecalculated)
                            .forEach(representatives::add);
                } else {
                    clusters.stream()
                            .map(Cluster::squashToCentroid)
//                            .map(Cluster::squashToMedian)
                            .forEach(representatives::add);
                }

                if (localForSvs.contains("add_random")) {
                    System.out.println("  [[FUTURE LOG]] Adding random representatives to clusters");
                    clusters.stream()
                            .map(cluster -> cluster.random(randomPercent, seed(paramProvider)))
                            .forEach(representatives::addAll);
                }
                break;
            }

            case "convex_hull_add_random":
            case "convex_hull": {
                kClusters = kGroupsParam(dataProvider, paramProvider, trainingSize);
                String initMethod = paramProvider.provide("init_kmeans_method", "k-means++");
                Collection<Cluster> clusters = new PartitioningClustering(kClusters, initMethod, paramProvider.distanceFunction(), debug(paramProvider))
                        .perform(labeledObservationsFiltered);

                clusters.stream()
                        .map(Cluster::getElements)
                        .map(s -> new ConvexHullLabeledFilter(paramProvider.distanceFunction(), s))
                        .map(ConvexHullLabeledFilter::process)
                        .forEach(representatives::addAll);

                if (localForSvs.contains("add_random")) {
                    System.out.println("  [[FUTURE LOG]] Adding random representatives to clusters");
                    clusters.stream()
                            .map(cluster -> cluster.random(randomPercent, seed(paramProvider)))
                            .forEach(representatives::addAll);
                }
            }

            case "centroid_with_metrics":
            case "centroids":
            default: {
                kClusters = kGroupsParam(dataProvider, paramProvider, trainingSize);
                String initMethod = paramProvider.provide("init_kmeans_method", "k-means++");
                Collection<Cluster> clusters = new PartitioningClustering(kClusters, initMethod, distanceFunction, debug(paramProvider)).perform(labeledObservationsFiltered);
                if ("centroid_with_metrics".equals(localForSvs)) {
                    clusters.stream()
                            .peek(Cluster::calculateMetrics)
                            .map(LabelledWrapper::ofPrecalculated)
                            .forEach(representatives::add);
                } else {
                    clusters.stream()
                            .map(Cluster::squashToCentroid)
//                            .map(Cluster::squashToMedian)
                            .forEach(representatives::add);
                }

                if (localForSvs.contains("add_random")) {
                    System.out.println("  [[FUTURE LOG]] Adding random representatives to clusters");
                    clusters.stream()
                            .map(cluster -> cluster.random(randomPercent, seed(paramProvider)))
                            .forEach(representatives::addAll);
                }
                break;
            }
        }

        System.out.println("  [[FUTURE LOG]] processLocal: representatives=" + representatives.size() + " of " + labeledObservationsFiltered.size());

        if (useLocalClassifier(paramProvider)) {
            LabeledObservation dummyObservation = LocalRepresentativesModel.dummyObservation();
            return new LocalRepresentativesModel(svmModel, Collections.singleton(dummyObservation), trainingSize, kClusters);
        } else {
            return new LocalRepresentativesModel(svmModel, representatives, trainingSize, kClusters);
        }
    }

    private int kGroupsParam(DataProvider dataProvider, ParamProvider paramProvider, int trainingSize) {
        int kClusters;
        kClusters = paramProvider.provideNumeric("groups", -1d)
                .intValue();
        int labelMultiplier = 1;
        if (kClusters == -2) {
            labelMultiplier = (int) dataProvider.training()
                    .stream()
                    .map(Data::getLabel)
                    .distinct()
                    .count();
            System.out.println("  [[FUTURE LOG]] Found " + labelMultiplier + " in " + trainingSize + " data samples");
        }
        if (kClusters <= 0) {
            kClusters = (int) Math.max(2, Math.ceil(Math.pow(Math.log(trainingSize), 2)));
            kClusters *= labelMultiplier;
            kClusters = (int) Math.min(kClusters, Math.ceil((float) trainingSize / 2));
            System.out.println("  [[FUTURE LOG]] Clusters amount calculated=" + kClusters);
        }
        return kClusters;
    }

    private LabeledObservation dataToObservation(Data d) {
        try { // FIXME int label/index
            return new LabeledObservation(Integer.parseInt(d.getId()), d.getNumericAttributes(), (int) Double.parseDouble(d.getLabel()));
        } catch (Exception e) {
            System.err.println(" [[FUTURE LOG]] Data row sample error for id: " + d.getId());
            return null;
        }
    }

    private List<LabeledObservation> toLabeledObservation(Collection<Data> training, ParamProvider paramProvider) {
        String filterMethod = paramProvider.provide("local_filtering_method");
        System.out.println("  [[FUTURE LOG]] local_filtering_method: " + filterMethod);

        if (filterMethod == null || "none".equals(filterMethod)) {
            return training.stream()
                    .map(this::dataToObservation)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } else {
            switch (filterMethod) {
                case "random": {
                    double percent = paramProvider.provideNumeric("local_filtering_percent");
                    Random rand = Optional.ofNullable(seed(paramProvider))
                            .map(Random::new)
                            .orElseGet(Random::new);
                    Collections.shuffle((List<Data>) training, rand);

                    int initialSize = training.size();
                    long limit = Math.max(1, (long) Math.ceil(initialSize * percent));
                    if (debug(paramProvider)) {
                        System.out.println("  [[FUTURE LOG]] Training set from size " + initialSize + " reduced randomly to " + limit + " elements");
                    }
                    return training.stream()
                            .limit(limit)
                            .map(this::dataToObservation)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                }
                case "class_random": {
                    double percent = paramProvider.provideNumeric("local_filtering_percent");
                    Random rand = Optional.ofNullable(seed(paramProvider))
                            .map(Random::new)
                            .orElseGet(Random::new);
                    return training.stream()
                            .collect(Collectors.groupingBy(Data::getLabel))
                            .values()
                            .stream()
                            .map(t -> {
                                int initialSize = t.size();
                                long limit = Math.max(1, (long) Math.ceil(initialSize * percent));
                                if (debug(paramProvider)) {
                                    System.out.println("  [[FUTURE LOG]] Training set class from size " + initialSize + " reduced randomly to " + limit + " elements");
                                }
                                Collections.shuffle(t, rand);
                                return t.stream()
                                        .limit(limit)
                                        .map(this::dataToObservation)
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList());
                            })
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
                }
                case "partitioning_random": {
                    List<LabeledObservation> labeled = training.stream()
                            .map(this::dataToObservation)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    int kClusters = paramProvider.provideNumeric("groups").intValue();
                    String initMethod = paramProvider.provide("init_kmeans_method", "k-means++");
                    Collection<Cluster> clusters = new PartitioningClustering(kClusters, initMethod, paramProvider.distanceFunction(), debug(paramProvider))
                            .perform(labeled);

                    double percent = paramProvider.provideNumeric("local_filtering_percent");
                    Random rand = Optional.ofNullable(seed(paramProvider))
                            .map(Random::new)
                            .orElseGet(Random::new);

                    return clusters.stream()
                            .map(Cluster::getElements)
                            .peek(clusterData->Collections.shuffle(clusterData, rand))
                            .map(clusterData -> {
                                int initialSize = clusterData.size();
                                long limit = Math.max(1, (long) Math.ceil(initialSize * percent));
                                if (debug(paramProvider)) {
                                    System.out.println("  [[FUTURE LOG]] Training cluster from size " + initialSize + " reduced randomly to " + limit + " elements");
                                }
                                return clusterData.stream()
                                        .limit(limit)
                                        .collect(Collectors.toList());
                            })
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
                }

                default:
                    throw new IllegalArgumentException("Unknown local_filtering_method method: " + filterMethod);
            }
        }
    }

    @Override
    public GlobalClassifier updateGlobal(Collection<LocalRepresentativesModel> localModels, ParamProvider paramProvider) {
        if (useLocalClassifier(paramProvider)) {
            return new GlobalClassifier(null, localModels.toArray(new LocalRepresentativesModel[]{}), new HashMap<>());
        }

        List<LabeledObservation> trainingSet = localModels.stream()
                .map(LocalRepresentativesModel::getRepresentatives)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        System.out.println("  [[FUTURE LOG]] updateGlobal: local trainSet=" + trainingSet.size());

        trainingSet.addAll(expandModels(localModels, paramProvider));
        System.out.println("  [[FUTURE LOG]] updateGlobal: expanded trainSet=" + trainingSet.size());

        String kernel = paramProvider.provide("kernel");
        WekaSVMClassification wekaClassifier = new WekaSVMClassification(kernel, seed(paramProvider));
        SVMModel svmModel = wekaClassifier.train(trainingSet);

        if (firstGlobalLevelOnly(paramProvider)) {
            System.out.println("  [[FUTURE LOG]] updateGlobal: trained with " + trainingSet.size() + " samples");

            return new GlobalClassifier(svmModel, null, null);
        } else {
            LocalRepresentativesModel[] localModelArray = localModels.toArray(new LocalRepresentativesModel[0]);
            Map<Integer, List<LabeledObservation>> classToLocalModel = new HashMap<>();
            List<String> labels = new ArrayList<>();
            for (int i = 0; i < localModelArray.length; i++) {
                for (LabeledObservation observation : localModelArray[i].getRepresentatives()) {
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

            return new GlobalClassifier(svmModel, localModelArray, knnModelMap);
        }
    }

    private List<LabeledObservation> expandModels(Collection<LocalRepresentativesModel> localModels, ParamProvider paramProvider) {
        String localForNonMulti = paramProvider.provide("local_method_name");
        if (!"centroid_with_metrics".equals(localForNonMulti)) {
            return Collections.emptyList();
        }

        double percent = paramProvider.provideNumeric("global_expand_percent", -0.1);
        boolean debug = debug(paramProvider);
        Random rand = Optional.ofNullable(seed(paramProvider))
                .map(Random::new)
                .orElseGet(Random::new);

        return localModels.stream()
                .map(LocalRepresentativesModel::getRepresentatives)
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
                .local(Svm2Lvl.class)
                .global(Svm2Lvl.class)
                .repeatLocal(Svm2Lvl.class)
                .lastGlobal(Svm2Lvl.class);
    }

    static boolean useLocalClassifier(ParamProvider paramProvider) {
        return Boolean.TRUE.toString().equals(paramProvider.provide("use_local_classifier", Boolean.FALSE.toString()));
    }

    static boolean firstGlobalLevelOnly(ParamProvider paramProvider) {
        return Boolean.TRUE.toString().equals(paramProvider.provide("use_first_global_level_only", "false"));
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
