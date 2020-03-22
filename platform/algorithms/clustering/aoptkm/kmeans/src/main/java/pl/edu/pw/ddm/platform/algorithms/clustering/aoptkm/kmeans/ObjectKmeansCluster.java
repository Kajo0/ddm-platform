package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.kmeans;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.distance.DistanceFunction;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.point.ObjectPoint;

public class ObjectKmeansCluster {

    public ObjectPoint centroid;
    public List<ObjectPoint> cluster = Lists.newArrayList();

    public void recalculateCentroid(DistanceFunction<Object[]> function) {
        if (!cluster.isEmpty()) {
            int clId = centroid.clusterId;
            List<Object[]> arr = cluster.stream().map(o -> o.values).collect(Collectors.toList());
            centroid = new ObjectPoint(function.meanMerge(arr), centroid.index);
            centroid.clusterId = clId;
        }
    }

}
