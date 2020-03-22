package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.optics;

import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.distance.DistanceFunction;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.distance.EuclideanHammingFunction;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.point.Point;

public class Optics {

    private Double eps;
    private Integer minPts;
    private List<ObjectOpticsPoint> points;
    private DistanceFunction distanceFunc = new EuclideanHammingFunction();
    private List<ObjectOpticsPoint> orderedResult = Lists.newArrayList();

    public Optics(Double eps, Integer minPts, List<ObjectOpticsPoint> points) {
        this.eps = eps;
        this.minPts = minPts;
        this.points = points;
    }

    public void optics() {
        Stopwatch timer = Stopwatch.createStarted();

        internalOptics();

        long elapsed = timer.stop().elapsed(TimeUnit.MILLISECONDS);
        if (System.getenv("LOG_TIME_OPTICS") != null) {
            System.out.println("    OPTICS Time elapsed: " + elapsed + " ms");
        }
    }

    private void internalOptics() {
        points.stream().filter(point -> !point.processed).forEach(this::expandClusterOrder);
    }

    private void expandClusterOrder(ObjectOpticsPoint point) {
        List<ObjectOpticsPoint> neighbors = neighbors(point);
        point.processed = true;
        point.reachabilityDistance = null;
        setCoreDistance(point, neighbors);
        orderedResult.add(point);
        if (point.coreDistance != null) {
            PriorityQueue<ObjectOpticsPoint> seeds =
                    new PriorityQueue<>((o1, o2) -> Double.compare(o1.reachabilityDistance, o2.reachabilityDistance));
            updateSeeds(neighbors, point, seeds);
            while (!seeds.isEmpty()) {
                ObjectOpticsPoint currentPoint = seeds.poll();
                neighbors = neighbors(currentPoint);
                currentPoint.processed = true;
                setCoreDistance(currentPoint, neighbors);
                orderedResult.add(currentPoint);
                if (currentPoint.coreDistance != null) {
                    updateSeeds(neighbors, currentPoint, seeds);
                }
            }
        }
    }

    private List<ObjectOpticsPoint> neighbors(ObjectOpticsPoint point) {
        return points.stream()
                .filter(setPoint -> point.distance(setPoint, distanceFunc) <= eps)
                .collect(Collectors.toList());
    }

    private void setCoreDistance(ObjectOpticsPoint point, List<ObjectOpticsPoint> neighbors) {
        if (neighbors.size() < minPts) {
            point.coreDistance = null;
        } else {
            List<ObjectOpticsPoint> sorted = neighbors.stream()
                    .sorted((o1, o2) -> point.distance(o1, distanceFunc) < point.distance(o2, distanceFunc) ? -1 : 0)
                    .collect(Collectors.toList());
            point.coreDistance = point.distance(sorted.get(minPts - 1), distanceFunc);
        }
    }

    private void updateSeeds(List<ObjectOpticsPoint> neighbors, ObjectOpticsPoint point,
                             PriorityQueue<ObjectOpticsPoint> seeds) {
        Double coreDistance = point.coreDistance;
        neighbors.stream().filter(setPoint -> !setPoint.processed).forEach(setPoint -> {
            double newReachableDistance = Math.max(coreDistance, point.distance(setPoint, distanceFunc));
            if (setPoint.reachabilityDistance == null) {
                setPoint.reachabilityDistance = newReachableDistance;
                seeds.add(setPoint);
            } else if (newReachableDistance < setPoint.reachabilityDistance) {
                // decrease
                seeds.remove(setPoint);
                setPoint.reachabilityDistance = newReachableDistance;
                seeds.add(setPoint);
            }
        });
    }

    public void extractDbscanClustering(double epsPrim) {
//        checkState(epsPrim < eps);
        int clusterId = Point.NOISE;
        for (ObjectOpticsPoint orderedPoint : orderedResult) {
            // Undefined > eps
            if (orderedPoint.reachabilityDistance == null || orderedPoint.reachabilityDistance > epsPrim) {
                if (orderedPoint.coreDistance != null && orderedPoint.coreDistance <= epsPrim) {
                    clusterId = Point.nextId(clusterId);
                    orderedPoint.clusterId = clusterId;
                } else {
                    orderedPoint.clusterId = Point.NOISE;
                }
            } else {
                orderedPoint.clusterId = clusterId;
            }
        }
    }

    public List<ObjectOpticsPoint> getOrderedResult() {
        return orderedResult;
    }

}
