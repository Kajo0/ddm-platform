package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.kmeans;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.distance.DistanceFunction;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.distance.EuclideanHammingFunction;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.point.ObjectPoint;

public class Kmeans {

    private Double epsilon;
    private Integer groups;
    private Integer iterations;
    private List<ObjectPoint> points;
    private DistanceFunction distanceFunc = new EuclideanHammingFunction();
    private List<ObjectKmeansCluster> clusters = Lists.newArrayList();

    public Kmeans(Double epsilon, Integer groups, Integer iterations, List<ObjectPoint> points) {
        this.epsilon = epsilon;
        this.groups = groups;
        this.iterations = iterations;
        this.points = points;
    }

    public void kmeans() {
        kmeans(null);
    }

    public void kmeans(List<ObjectPoint> centroids) {
        // TODO restore after guava collisions fixed
//        Stopwatch timer = Stopwatch.createStarted();

        internalKmeans(centroids);

//        long elapsed = timer.stop().elapsed(TimeUnit.MILLISECONDS);
//        if (System.getenv("LOG_TIME_KMEANS") != null) {
//            System.out.println("    K-means Time elapsed: " + elapsed + " ms");
//        }
    }

    private void internalKmeans(List<ObjectPoint> centroids) {
        Preconditions.checkState(groups > 0);

        clusters.clear();
        if (centroids == null) {
            centroids = findRandomCentroids();
        }
        clusters.addAll(prepareCentroidClusters(centroids));

        assignPoints(points, clusters, distanceFunc);

        double error = Double.MAX_VALUE;
        for (int iteration = 0; iteration != iterations; ++iteration) {
            recalculateCentroids();
            if (!reassignPoints(clusters, distanceFunc)) {
                break;
            }
            double qError = error;
            error = calculateQuantizationError();
            if (error >= 0) {
                qError = (qError - error) / error;
            }
            if (qError < epsilon) {
                break;
            }
        }
    }

    public static List<ObjectKmeansCluster> prepareCentroidClusters(List<ObjectPoint> centroids) {
        return centroids.stream().map(p -> {
            ObjectKmeansCluster cluster = new ObjectKmeansCluster();
            cluster.centroid = new ObjectPoint(p.values, p.index);
            cluster.centroid.clusterId = p.clusterId;
            return cluster;
        }).collect(Collectors.toList());
    }

    private List<ObjectPoint> findRandomCentroids() {
        Collections.shuffle(points);
        List<ObjectPoint> centroids = Lists.newArrayList();
        for (int i = 0; i < Math.min(groups, points.size()); ++i) {
            ObjectPoint c = (ObjectPoint) deepCopy(points.get(i));
            c.clusterId = i;
            centroids.add(c);
        }
        return centroids;
    }

    public static void assignPoints(List<ObjectPoint> points, List<ObjectKmeansCluster> clusters,
                                    DistanceFunction distanceFunc) {
        for (ObjectPoint point : points) {
            double minDistance = Double.MAX_VALUE;
            ObjectKmeansCluster closest = null;
            for (ObjectKmeansCluster cluster : clusters) {
                double distance = cluster.centroid.distance(point, distanceFunc);
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = cluster;
                }
            }
            Preconditions.checkNotNull(closest);
            point.clusterId = closest.centroid.clusterId;
            closest.cluster.add(point);
        }
    }

    private void recalculateCentroids() {
        clusters.forEach(c -> c.recalculateCentroid(distanceFunc));
    }

    /**
     * @return true if at lest one point has changed assignment
     */
    public static boolean reassignPoints(List<ObjectKmeansCluster> clusters, DistanceFunction distanceFunc) {
        List<ObjectPoint> points = Lists.newArrayList();
        for (ObjectKmeansCluster cluster : clusters) {
            points.addAll(cluster.cluster);
            cluster.cluster.clear();
        }
        assignPoints(points, clusters, distanceFunc);
        // TODO think about - different approach
        return true;
    }

    private double calculateQuantizationError() {
        int points = 0;
        double result = 0;
        for (ObjectKmeansCluster cluster : clusters) {
            points += cluster.cluster.size();
            for (ObjectPoint point : cluster.cluster) {
                result += point.distance(cluster.centroid, distanceFunc);
            }
        }
        if (points == 0) {
            return Double.MAX_VALUE;
        } else {
            return result / points;
        }
    }

    private Object deepCopy(Object obj) {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
            ois = new ObjectInputStream(bin);
            return ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert oos != null;
                oos.close();
                assert ois != null;
                ois.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public List<ObjectKmeansCluster> getClusters() {
        return clusters;
    }

}
