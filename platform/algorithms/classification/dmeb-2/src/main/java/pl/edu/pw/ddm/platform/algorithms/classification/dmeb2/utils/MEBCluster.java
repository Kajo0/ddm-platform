package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;


public class MEBCluster implements Serializable {
    private Centroid centroid;
    private List<LabeledObservation> clusterElementList;

    public Centroid getCentroid() {
        return centroid;
    }

    public List<LabeledObservation> getClusterElementList() {
        return clusterElementList;
    }

    public void setCentroid(Centroid centroid) {
        this.centroid = centroid;
    }

    public void setClusterElementList(List<LabeledObservation> clusterElementList) {
        this.clusterElementList = clusterElementList;
    }

    public boolean containsAny(List<LabeledObservation> representativeList) {
        for (LabeledObservation observation : representativeList) {
            double[] features = Arrays.copyOfRange(observation.getFeatures(), 0, observation.getFeatures().length);
            if (Arrays.equals(features, centroid.getFeatures())) {
                return true;
            }
            for (LabeledObservation ce : clusterElementList) {
                if (Arrays.equals(features, ce.getFeatures())) {
                    return true;
                }
            }
        }
        return false;
    }
}
