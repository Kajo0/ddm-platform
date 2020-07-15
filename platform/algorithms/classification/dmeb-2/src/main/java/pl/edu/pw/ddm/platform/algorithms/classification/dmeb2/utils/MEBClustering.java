package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;

@RequiredArgsConstructor
public class MEBClustering {

    private final int mebClusters;

    public MEBModel perform(List<LabeledObservation> data, Integer partitionId) {
        try {
            SimpleKMeans mySKMeans = new SimpleKMeans();
            mySKMeans.setNumClusters(mebClusters);
            Instances ddata = WekaUtils.convertClusteringToInstances2(data);
            mySKMeans.setPreserveInstancesOrder(true);
            mySKMeans.buildClusterer(ddata);
            Instances clusterCentroids = mySKMeans.getClusterCentroids();
            Map<Integer, MEBCluster> clusters = new HashMap<>();
            for (int i = 0; i < data.size(); ++i) {
                LabeledObservation observation = data.get(i);
                int clusterId = mySKMeans.clusterInstance(ddata.instance(i)); // kj
                MEBCluster mebCluster = clusters.get(clusterId);
                if (mebCluster == null) {
                    mebCluster = new MEBCluster();
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