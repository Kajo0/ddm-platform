package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm;

import com.google.common.collect.Lists;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.impl.GModel;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.impl.LModel;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.impl.PredefinedDistanceFuncWrapper;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.kmeans.Kmeans;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.kmeans.ObjectKmeansCluster;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.optics.ObjectOpticsPoint;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.distributed.DistributedCentroidData;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.distributed.DistributedUtils;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.point.ObjectPoint;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AutoOpticsKm extends OpticsDkm {

    public AutoOpticsKm(ParamProvider paramProvider) {
        super(paramProvider);

        groups = paramProvider.provideNumeric("groups", 2.).intValue();
        iterations = paramProvider.provideNumeric("iterations", 10.).intValue();
        epsilon = paramProvider.provideNumeric("epsilon", 0.1);
        indexNumber = paramProvider.provideNumeric("indexNumber", 0.).intValue();
        labelNumber = paramProvider.provideNumeric("labelNumber", 1.).intValue();
        splitPattern = paramProvider.provide("splitPattern", ",");

        distanceFunc = new PredefinedDistanceFuncWrapper(paramProvider);

        inputPath = null;
        outputPath = "/tmp";

        increaseModel = paramProvider.provideNumeric("increaseModel", null);
        noOneGroup = Boolean.parseBoolean(paramProvider.provide("noOneGroup", "false"));
        minKGroups = Boolean.parseBoolean(paramProvider.provide("minKGroups", "false"));
        exactKGroups = Boolean.parseBoolean(paramProvider.provide("exactKGroups", "false"));

        printConfig();
    }

    // TODO remove debug checker
    private void printConfig() {
        System.out.println("---------------------------------");
        System.out.println("-        AOPTKM - CONFIG        -");
        System.out.println("---------------------------------");
        System.out.println("  groups        = " + groups);
        System.out.println("  iterations    = " + iterations);
        System.out.println("  epsilon       = " + epsilon);
        System.out.println("  indexNumber   = " + indexNumber);
        System.out.println("  labelNumber   = " + labelNumber);
        System.out.println("  splitPattern  = " + splitPattern);
        System.out.println("  distanceFunc  = " + distanceFunc);
        System.out.println("  inputPath     = " + inputPath);
        System.out.println("  outputPath    = " + outputPath);
        System.out.println("  increaseModel = " + increaseModel);
        System.out.println("  noOneGroup    = " + noOneGroup);
        System.out.println("  minKGroups    = " + minKGroups);
        System.out.println("  exactKGroups  = " + exactKGroups);
        System.out.println("---------------------------------");
    }

    public AutoOpticsKm(String[] args) {
        super(args);
    }

    @Override
    public void run() {
        List<LModel> localModels = Lists.newArrayList();
        List<List<ObjectPoint>> localAdditionalPoints = Lists.newArrayList();

        Map<String, List<ObjectPoint>> nodes = readNodes();
        // TODO FIXME remove
        List<ObjectPoint> allPoints = Lists.newArrayList();
        nodes.forEach((key, value) -> allPoints.addAll(value));
        // TODO FIXME remove

        // step 1.1 : local clustering
        nodes.entrySet()
                .stream()
                .map(entry -> localClustering(entry.getKey(), entry.getValue()))
                .forEach(lm -> {
                    localModels.add(lm);
//                    localAdditionalPoints.add(getRandomAdditionalPoints(entry.getValue()));
                    localAdditionalPoints.add(lm.getAdditionalPointsNearCentroids());
                });

        // step 3 : create global model
        GModel globalModel = globalClustering(localModels);
        writeGlobalResult(allPoints, globalModel.getCentroids(), true);

        // step 4 : assign local points to global clusters
        nodes.forEach((nodeName, pts) -> updateLocalClustering(pts, globalModel.getCentroids()));
        writeLocalResults(nodes, globalModel.getCentroids());

        writeCentroids(globalModel.getCentroids());
    }

    public LModel localClustering(String nodeName, List<ObjectPoint> pts) {
        return localClustering(nodeName, pts, false, null);
    }

    public LModel localClustering(String nodeName,
                                  List<ObjectPoint> pts,
                                  boolean wekaInit,
                                  ParamProvider paramProvider) {
        // step 1.3 : run clustering alg. on local data
        startLog("AOPTKM local kmeans");
        List<ObjectKmeansCluster> calcClusters = runKmeans(pts, null, wekaInit, paramProvider);
        stopLog(null);
//        writePreLocalResult(nodeName, calcClusters);

        // step 2 : create local model
        List<DistributedCentroidData> model = Lists.newArrayList();
        model.addAll(calcClusters.stream().map(c -> {
            DistributedCentroidData data = DistributedUtils.calculateData(c.cluster, c.centroid, distanceFunc);
            data.setValue(data.getM());
            return data;
        }).collect(Collectors.toList()));

        return new LModel(model, getOrderedAdditionalPoints(calcClusters));
    }

    public List<ObjectKmeansCluster> updateLocalClustering(List<ObjectPoint> pts, List<ObjectPoint> globalCentroids) {
        List<ObjectKmeansCluster> rClusters = Kmeans.prepareCentroidClusters(globalCentroids);
        startLog("common local update");
        Kmeans.assignPoints(pts, rClusters, distanceFunc);
        stopLog(null);
//        writeResult(rClusters, outputPath + "_" + "dummyNodeName");
        return rClusters;
    }

    public GModel globalClustering(List<LModel> localModels) {
        List<ObjectPoint> mergedCentroids = Lists.newArrayList();
        final double[] maxMeanSd = {0, 0, 0};
        final int[] minus = {0};
        localModels.forEach(local -> local.getCentroids().forEach(data -> {
            if (data.getN() > 1 && !Double.isNaN(data.getSD())) {
                mergedCentroids.add(new ObjectPoint(data.getCentroid()));
                maxMeanSd[0] += data.getMax();
                maxMeanSd[1] += data.getM();
                maxMeanSd[2] += data.getSD();
            } else {
                minus[0]++;
            }
        }));
        int div = Math.max(1, mergedCentroids.size() - minus[0]);
        for (int i = 0; i < maxMeanSd.length; ++i) {
            maxMeanSd[i] /= div;
        }

        localModels.forEach(local -> mergedCentroids.addAll(local.getAdditionalPointsNearCentroids()));

        // step 3.1 : run global optics on local models
        eps = maxMeanSd[0];
        minPts = 1;
        epsPrim = eps - maxMeanSd[2];
        System.out.println(String.format(" [max=%f, mean=%f, stddev=%f] [eps=%f, eps'=%f]", maxMeanSd[0], maxMeanSd[1],
                maxMeanSd[2], eps, epsPrim));

        List<ObjectPoint> recalcCentroids = recalculateCentroids(mergedCentroids);
        // step 3.1.1 : preprocess global centroids
        if (noOneGroup && recalcCentroids.size() == 1) {
            recalcCentroids = findMinGroups(recalcCentroids, mergedCentroids, 2, maxMeanSd);
        } else if (minKGroups && recalcCentroids.size() < groups) {
            recalcCentroids = findMinGroups(recalcCentroids, mergedCentroids, groups, maxMeanSd);
        }

        if (exactKGroups && recalcCentroids.size() > groups) {
            while (recalcCentroids.size() != groups) {
                recalcCentroids = mergeClosest(recalcCentroids);
            }
            // K-means merging
//            recalcCentroids = runKmeans(recalcCentroids, null).stream()
//                    .map(cluster -> cluster.centroid)
//                    .collect(Collectors.toList());
        }

//        // step 3.2 : merge auto found groups
//        List<List<DistributedCentroidData>> list = Lists.newArrayList();
//        recalcCentroids.forEach(c -> {
//            DistributedCentroidData data = new DistributedCentroidData();
//            data.setCentroid(c.values);
//            data.setValue(2 * maxMeanSd[1]);
//            list.add(Lists.newArrayList(data));
//        });
//        recalcCentroids = DistributedUtils.mergeCentroids(list, distanceFunc);

        return new GModel(recalcCentroids);
    }

    private List<ObjectPoint> mergeClosest(List<ObjectPoint> recalcCentroids) {
        List<List<Object>> list = Lists.newArrayList();
        for (int i = 0; i < recalcCentroids.size(); ++i) {
            ObjectPoint point = recalcCentroids.get(i);
            for (int j = 0; j < recalcCentroids.size(); ++j) {
                if (i != j) {
                    ObjectPoint another = recalcCentroids.get(j);
                    double distance = point.distance(another, distanceFunc);
                    list.add(Lists.newArrayList(distance, point, another));
                }
            }
        }

        list.sort(Comparator.comparingDouble(a -> (Double) a.get(0)));
        Pair<ObjectPoint, ObjectPoint> pair =
                new Pair<>((ObjectPoint) list.get(0).get(1), (ObjectPoint) list.get(0).get(2));

        List<ObjectPoint> result = Lists.newArrayList();
        for (ObjectPoint point : recalcCentroids) {
            if (!point.equals(pair.getKey()) && !point.equals(pair.getValue())) {
                result.add(point);
            }
        }

        ObjectPoint centroid = new ObjectPoint(distanceFunc.meanMerge(Lists.newArrayList(pair.getKey().values, pair.getValue().values)));
        centroid.clusterId = pair.getKey().clusterId;
        result.add(centroid);

        return result;
    }


    private List<ObjectPoint> findMinGroups(List<ObjectPoint> recalcCentroids, List<ObjectPoint> mergedCentroids,
                                            Integer groups, double[] maxMeanSd) {
        int i = 0;
        double partition = maxMeanSd[1] / maxMeanSd[0];
        partition += (1 - partition) / 2;

        while (recalcCentroids.size() < groups && i <= 30) {
            //eps = eps / 4 * 3; //try to not change that
            epsPrim = epsPrim * partition; //epsPrim / 4 * 3;
            System.out.println(String.format("   [eps=%f, eps'=%f] no. %d", eps, epsPrim, ++i));

            recalcCentroids = recalculateCentroids(mergedCentroids);
        }

        return recalcCentroids;
    }

    private List<ObjectPoint> recalculateCentroids(List<ObjectPoint> mergedCentroids) {
        List<ObjectOpticsPoint> oPts = convertToOpticsPoints(mergedCentroids);

        startLog("AOPTKM global optics");
        List<ObjectPoint> recalcCentroids = runOpticsForCentroids(oPts, true);
        stopLog(null);

        return recalcCentroids;
    }

    static class Pair<K, V> {

        private final K key;
        private final V value;

        Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }

}
