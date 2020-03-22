package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.distributed;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.distance.DistanceFunction;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.point.ObjectPoint;

public class DistributedUtils {

    public static DistributedCentroidData calculateData(List<ObjectPoint> points, ObjectPoint center,
                                                        DistanceFunction<Object[]> distanceFunc) {
        final DistributedCentroidData data = new DistributedCentroidData();
        data.setN(points.size());
        data.setCentroid(center.values);
        data.setMin(Double.MAX_VALUE);
        data.setMax(Double.MIN_VALUE);

        points.forEach(p -> {
            double distance = distanceFunc.distance(center.values, p.values);
            data.setS(data.getS() + distance);
            data.setSS(data.getSS() + distance * distance);
            if (data.getMin() > distance) {
                data.setMin(distance);
            }
            if (data.getMax() < distance) {
                data.setMax(distance);
            }
        });

        data.setM(data.getS() / data.getN());
        data.setSD(Math.pow((data.getSS() - (2 * data.getM() * data.getS()) + (data.getN() * data.getM() * data.getM()))
                / data.getN(), 0.5));

        return data;
    }

    public static List<ObjectPoint> mergeCentroids(List<List<DistributedCentroidData>> clusters,
                                                   DistanceFunction<Object[]> distanceFunc) {
        Map<DistributedCentroidData, DistributedCentroidData> alreadyMerged = Maps.newHashMap();
        Map<DistributedCentroidData, List<DistributedCentroidData>> toMerge = Maps.newHashMap();
        List<DistributedCentroidData> notMerged = Lists.newArrayList();
        clusters.forEach(notMerged::addAll);

        int resCount = 0;
        for (int i = 0; i < clusters.size() - 1; ++i) {
            List<DistributedCentroidData> ci = clusters.get(i);
            for (int ii = 0; ii < ci.size(); ++ii) {
                DistributedCentroidData c1 = ci.get(ii);
                for (int j = i + 1; j < clusters.size(); ++j) {
                    List<DistributedCentroidData> cj = clusters.get(j);
                    for (int jj = 0; jj < cj.size(); ++jj) {
                        DistributedCentroidData c2 = cj.get(jj);
                        double dist = distanceFunc.distance(c1.getCentroid(), c2.getCentroid());
//                        System.out.println(
//                                String.format("L %d I %d merge with L %d I %d wynik %b, (c1=%s, c2=%s)", i, ii, j, jj,
//                                        NdkmUtils.checkMergeCondition(dist, c1.getValue(), c2.getValue()),
//                                        Arrays.toString(c1.getCentroid()), Arrays.toString(c2.getCentroid())));
                        if (DistributedUtils.checkMergeCondition(dist, c1.getValue(), c2.getValue())) {
                            if (notMerged.contains(c1)) {
                                notMerged.remove(c1);
                                notMerged.remove(c2);

                                if (alreadyMerged.containsKey(c2)) {
                                    DistributedCentroidData baseC2 = alreadyMerged.get(c2);
                                    List<DistributedCentroidData> toMergeC1 = toMerge.get(baseC2);
                                    if (!toMergeC1.contains(c1)) {
                                        toMergeC1.add(c1);
                                    }
                                } else {
                                    handleMergeLists(alreadyMerged, toMerge, c1, c2);
                                }
                            } else if (notMerged.contains(c2)) {
                                notMerged.remove(c2);
                                DistributedCentroidData baseC1 = c1;
                                while (alreadyMerged.containsKey(baseC1)) {
                                    baseC1 = alreadyMerged.get(baseC1);
                                }
                                handleMergeLists(alreadyMerged, toMerge, baseC1, c2);
                            }
                            resCount++;
                        }
                    }
                }
            }
        }

        System.out.println("ResCount=" + resCount);
        return mergeLocalClustersIntoGlobal(toMerge, notMerged, distanceFunc);
    }

    private static void handleMergeLists(Map<DistributedCentroidData, DistributedCentroidData> alreadyMerged,
                                         Map<DistributedCentroidData, List<DistributedCentroidData>> toMerge, DistributedCentroidData baseC1,
                                         DistributedCentroidData c2) {
        List<DistributedCentroidData> toMergeC1 = toMerge.get(baseC1);
        if (toMergeC1 == null) {
            toMergeC1 = Lists.newArrayList();
            toMerge.put(baseC1, toMergeC1);
        }
        if (!toMergeC1.contains(c2)) {
            toMergeC1.add(c2);
        }
        if (!alreadyMerged.containsKey(c2)) {
            alreadyMerged.put(c2, baseC1);
        }
    }

    private static List<ObjectPoint> mergeLocalClustersIntoGlobal(
            Map<DistributedCentroidData, List<DistributedCentroidData>> toMerge,
            List<DistributedCentroidData> notMerged, DistanceFunction<Object[]> distanceFunc) {
        List<ObjectPoint> centroids = Lists.newArrayList();

        final int[] clId = {0};
        centroids.addAll(notMerged.stream().map(data -> {
            ObjectPoint point = new ObjectPoint(data.getCentroid());
            point.clusterId = clId[0]++;
            return point;
        }).collect(Collectors.toList()));

        centroids.addAll(toMerge.entrySet().stream().map(entry -> {
            List<Object[]> list =
                    entry.getValue().stream().map(DistributedCentroidData::getCentroid).collect(Collectors.toList());
            list.add(entry.getKey().getCentroid());

            ObjectPoint point = new ObjectPoint(distanceFunc.meanMerge(list), 0);
            point.clusterId = clId[0]++;

            return point;
        }).collect(Collectors.toList()));

        return centroids;
    }

    public static boolean checkMergeCondition(double centroidDistance, double standardDeviation1,
                                              double standardDeviation2) {
        return centroidDistance <= Math.max(standardDeviation1, standardDeviation2);
    }

}
