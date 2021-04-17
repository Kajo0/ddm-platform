package pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.Tag;

public class PartitioningClustering {

    private final int kClusters;
    private final DistanceFunction distanceFunction;
    private final SelectedTag initMethod;
    private final boolean debug;

    public PartitioningClustering(int kClusters, String initMethod, DistanceFunction distanceFunction, boolean debug) {
        this.kClusters = kClusters;
        this.distanceFunction = distanceFunction;
        this.initMethod = findSelectedTag(initMethod);
        this.debug = debug;
    }

    private SelectedTag findSelectedTag(String initMethod) {
        for (Tag tag : SimpleKMeans.TAGS_SELECTION) {
            if (tag.getReadable().equals(initMethod)) {
                System.out.println("  [[FUTURE LOG]] Found tag method for '" + initMethod + "'");
                return new SelectedTag(tag.getID(), SimpleKMeans.TAGS_SELECTION);
            }
        }
        System.out.println("  [[FUTURE LOG]] Not found tag method for '" + initMethod + "' so using default.");
        return null;
    }

    public Collection<Cluster> perform(List<LabeledObservation> data) {
        try {
            SimpleKMeans mySKMeans = new SimpleKMeans();
            mySKMeans.setNumClusters(kClusters);
            Instances ddata = WekaUtils.convertClusteringToInstances2(data);
            mySKMeans.setPreserveInstancesOrder(true);
            if (initMethod != null) {
                mySKMeans.setInitializationMethod(initMethod);
            }
            mySKMeans.buildClusterer(ddata);
            Instances clusterCentroids = mySKMeans.getClusterCentroids();
            Map<Integer, Cluster> clusters = new HashMap<>();
            for (int i = 0; i < data.size(); ++i) {
                LabeledObservation observation = data.get(i);
                int clusterId = mySKMeans.clusterInstance(ddata.instance(i));
                Cluster cluster = clusters.get(clusterId);
                if (cluster == null) {
                    cluster = new Cluster(distanceFunction, debug);
                    cluster.setElements(new ArrayList<>());
                }
                cluster.getElements().add(observation);
                clusters.put(clusterId, cluster);
            }
            for (int i = 0; i < clusterCentroids.size(); i++) {
                clusters.get(i).setCentroid(new Centroid(clusterCentroids.get(i).toDoubleArray()));
            }
            return clusters.values();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
