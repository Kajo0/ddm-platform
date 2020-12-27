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

    public LabeledObservation squashToCentroid() {
        LabeledObservation any = clusterElementList.get(0);
        clusterElementList.clear();
        LabeledObservation squashed = new LabeledObservation(-1, centroid.getFeatures(), any.getTarget());
        clusterElementList.add(squashed);
        return squashed;
    }

}
