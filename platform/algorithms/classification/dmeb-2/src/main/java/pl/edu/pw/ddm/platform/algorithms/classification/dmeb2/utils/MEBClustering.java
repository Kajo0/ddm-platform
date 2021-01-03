package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.Tag;

public class MEBClustering {

    private final int mebClusters;
    private final DistanceFunction distanceFunction;
    private final SelectedTag initMethod;
    private final boolean debug;

    public MEBClustering(int mebClusters, String initMethod, DistanceFunction distanceFunction, boolean debug) {
        this.mebClusters = mebClusters;
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

    public MEBModel perform(List<LabeledObservation> data, Integer partitionId) {
        try {
            SimpleKMeans mySKMeans = new SimpleKMeans();
            mySKMeans.setNumClusters(mebClusters);
            Instances ddata = WekaUtils.convertClusteringToInstances2(data);
            mySKMeans.setPreserveInstancesOrder(true);
            if (initMethod != null) {
                mySKMeans.setInitializationMethod(initMethod);
            }
            mySKMeans.buildClusterer(ddata);
            Instances clusterCentroids = mySKMeans.getClusterCentroids();
            Map<Integer, MEBCluster> clusters = new HashMap<>();
            for (int i = 0; i < data.size(); ++i) {
                LabeledObservation observation = data.get(i);
                int clusterId = mySKMeans.clusterInstance(ddata.instance(i)); // kj
                MEBCluster mebCluster = clusters.get(clusterId);
                if (mebCluster == null) {
                    mebCluster = new MEBCluster(distanceFunction, debug);
                    mebCluster.setClusterElementList(new ArrayList<>());
                }
                mebCluster.getClusterElementList().add(observation);
                clusters.put(clusterId, mebCluster);
            }
            for (int i = 0; i < clusterCentroids.size(); i++) {
                clusters.get(i).setCentroid(new Centroid(clusterCentroids.get(i).toDoubleArray()));
            }
            return new MEBModel(new ArrayList<>(clusters.values()), partitionId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
