package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.kmeans.ObjectKmeansCluster;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.optics.ObjectOpticsPoint;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.optics.Optics;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.point.ObjectPoint;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.point.Point;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;

public class OpticsDkm extends Base {

    protected double eps;
    protected int minPts;
    protected double epsPrim;

    public OpticsDkm(ParamProvider paramProvider) {
        eps = paramProvider.provideNumeric("eps", 0.);
        minPts = paramProvider.provideNumeric("minPts", 0.).intValue();
        epsPrim = paramProvider.provideNumeric("epsPrim", 0.);
    }

    public OpticsDkm(String[] args) {
        super(args);

        eps = Double.valueOf(prop.getProperty("eps"));
        minPts = Integer.valueOf(prop.getProperty("minPts"));
        epsPrim = Double.valueOf(prop.getProperty("epsPrim"));
    }

    @Override
    public void run() {
        List<List<ObjectPoint>> localModelsCentroids = Lists.newArrayList();
        List<List<ObjectPoint>> localAdditionalPoints = Lists.newArrayList();

        Map<String, List<ObjectPoint>> nodes = readNodes();
        // step 0 : normalize local data
//        nodes = normalizeNodes(nodes);


        // TODO FIXME remove
        List<ObjectPoint> allPoints = Lists.newArrayList();
        nodes.entrySet().forEach(entry -> allPoints.addAll(entry.getValue()));
        // TODO FIXME remove


        // step 1.1 : local clustering
        nodes.entrySet().forEach(entry -> {
            String nodeName = entry.getKey();
            List<ObjectPoint> pts = entry.getValue();

            // step 1.2 : optics local
            List<ObjectOpticsPoint> onPts = convertToOpticsPoints(pts);
            List<ObjectPoint> centroids = runOpticsForCentroids(onPts, false);

            // step 1.3 : run clustering alg. on local data
            List<ObjectKmeansCluster> calcClusters = runKmeans(pts, centroids);
            writePreLocalResult(nodeName, calcClusters);

            // step 2 : create local model
            localModelsCentroids.add(calcClusters.stream().map(c -> c.centroid).collect(Collectors.toList()));


//            localAdditionalPoints.add(getRandomAdditionalPoints(pts));
            localAdditionalPoints.add(getOrderedAdditionalPoints(calcClusters));
        });

        // step 3 : create global model
        List<ObjectPoint> mergedCentroids = Lists.newArrayList();
        localModelsCentroids.forEach(mergedCentroids::addAll);


        localAdditionalPoints.forEach(mergedCentroids::addAll);


        List<ObjectKmeansCluster> globalClusters = runKmeans(mergedCentroids, null);
        List<ObjectPoint> recalcCentroids = prepareRecalcCentroids(globalClusters);

        writeGlobalResult(allPoints, recalcCentroids);

        // step 4 : assign local points to global clusters
        writeLocalResults(nodes, recalcCentroids);

        writeCentroids(recalcCentroids);
    }

    protected List<ObjectOpticsPoint> convertToOpticsPoints(List<ObjectPoint> pts) {
        return pts.stream().map(point -> new ObjectOpticsPoint(point.values, point.index)).collect(Collectors.toList());
    }

    protected List<ObjectPoint> runOpticsForCentroids(List<ObjectOpticsPoint> onPts, boolean meanCenterCentroid) {
        Optics optics = new Optics(eps, minPts, onPts);
        optics.optics();
        optics.extractDbscanClustering(epsPrim);
        Map<Integer, List<ObjectPoint>> map = mapOrderedResult(optics);
        System.out.println("Found groups: " + map.size());

        List<ObjectPoint> centroids = Lists.newArrayList();
        final int[] clId = {0};
        final ObjectPoint[] noise = {null};
        map.entrySet().stream().forEach(er -> {
            List<ObjectPoint> innerPts = er.getValue();
            if (!innerPts.isEmpty()) {
                if (er.getKey() != Point.NOISE) {
                    ObjectPoint point;
                    if (meanCenterCentroid) {
                        List<Object[]> arr = innerPts.stream().map(o -> o.values).collect(Collectors.toList());
                        point = new ObjectPoint(distanceFunc.meanMerge(arr));
                    } else {
                        int r = new Random().nextInt(innerPts.size());
                        point = new ObjectPoint(innerPts.get(r).values);
                    }
                    point.clusterId = clId[0]++;
                    centroids.add(point);
                } else {
                    noise[0] = new ObjectPoint(innerPts.get(0).values);
                    noise[0].clusterId = clId[0]++;
                }
            }
        });
        if (centroids.isEmpty() && noise[0] != null) {
            centroids.add(noise[0]);
        }
        return centroids;
    }

    protected Map<Integer, List<ObjectPoint>> mapOrderedResult(Optics optics) {
        Map<Integer, List<ObjectPoint>> map = Maps.newHashMap();
        optics.getOrderedResult().stream().forEach(point -> {
            List<ObjectPoint> cluster = map.get(point.clusterId);
            if (cluster == null) {
                cluster = Lists.newArrayList();
                map.put(point.clusterId, cluster);
            }
            cluster.add(point);
        });
        return map;
    }

}
