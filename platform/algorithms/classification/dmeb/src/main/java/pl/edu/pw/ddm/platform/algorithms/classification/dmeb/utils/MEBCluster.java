package pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;

@Getter
@Setter
public class MEBCluster implements Serializable {

    private transient boolean debug;
    private transient DistanceFunction distanceFunction;

    private Centroid centroid;
    private List<LabeledObservation> clusterElementList;

    // TODO make use of the function after removing weka impl.
    public MEBCluster(DistanceFunction distanceFunction, boolean debug) {
        this.distanceFunction = distanceFunction;
        this.debug = debug;
    }

    public boolean containsAny(Collection<LabeledObservation> representativeList) {
        for (LabeledObservation observation : representativeList) {
            if (Arrays.equals(observation.getFeatures(), centroid.getFeatures())) {
                return true;
            }
            for (LabeledObservation ce : clusterElementList) {
                if (Arrays.equals(observation.getFeatures(), ce.getFeatures())) {
                    return true;
                }
            }
        }
        return false;
    }

    public LabeledObservation squashToCentroid() {
        int count = clusterElementList.size();
        LabeledObservation any = clusterElementList.get(0);
        clusterElementList.clear();
        LabeledObservation squashed = new LabeledObservation(-1, centroid.getFeatures(), any.getTarget());
        clusterElementList.add(squashed);
        if (debug) {
            System.out.println("  [[FUTURE LOG]] Cluster with " + count + " elements squashed into 1");
        }
        return squashed;
    }

}
