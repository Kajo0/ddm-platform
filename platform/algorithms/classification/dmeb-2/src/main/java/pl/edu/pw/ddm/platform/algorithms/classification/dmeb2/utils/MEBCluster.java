package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import pl.edu.pw.ddm.platform.distfunc.EuclideanDistance;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;

@Getter
@Setter
public class MEBCluster implements Serializable {

    private Centroid centroid;
    private List<LabeledObservation> clusterElementList;

    public LabeledObservation squashToCentroid() {
        LabeledObservation any = clusterElementList.get(0);
        clusterElementList.clear();
        LabeledObservation squashed = new LabeledObservation(-1, centroid.getFeatures(), any.getTarget());
        clusterElementList.add(squashed);
        return squashed;
    }

    // TODO make use of the function after removing weka impl.
    public List<LabeledObservation> leaveBorder(DistanceFunction distanceFunction) {
        EuclideanDistance distanceFunc = new EuclideanDistance();
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double S = 0;
        double SS = 0;
        int N = clusterElementList.size();
        LabeledObservation closest = null;

        for (LabeledObservation p : clusterElementList) {
            double distance = distanceFunc.distance(centroid.getFeatures(), p.getFeatures());
            S += distance;
            SS += distance * distance;
            if (max < distance) {
                max = distance;
            }
            if (min > distance) {
                min = distance;
                closest = p;
            }
        }

        double M = S / N;
        double standardDeviation = Math.pow((SS - (2 * M * S) + (N * M * M)) / N, 0.5);
        System.out.println(
                "  [[FUTURE LOG]] Cluster with " + N + " elements [max=" + max + ",sd=" + standardDeviation + "]");

        double threshold = max - standardDeviation;
        clusterElementList.removeIf(p -> distanceFunc.distance(centroid.getFeatures(), p.getFeatures()) < threshold);
        if (closest != null && !clusterElementList.contains(closest)) {
            clusterElementList.add(closest);
            System.out.println("  [[FUTURE LOG]] Adding closest observation");
        }
        System.out.println(
                "  [[FUTURE LOG]] Cluster with " + N + " reduced to " + clusterElementList.size() + " elements");

        return clusterElementList;
    }

}
