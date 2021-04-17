package pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabelledWrapper extends LabeledObservation {

    private int elements;
    private double clusterSum;
    private double clusterMax;
    private double clusterStddev;
    private double clusterMean;
    private double[] mean;
    private double[] stddev;
    private double[] min;
    private double[] max;
    private double clusterDensityPercent;

    public LabelledWrapper(LabeledObservation observation) {
        super(observation.getIndex(), observation.getFeatures(), observation.getTarget());
    }

    public static LabelledWrapper ofPrecalculated(Cluster cluster) {
        LabelledWrapper lw = new LabelledWrapper(cluster.getElements()
                .stream()
                .findFirst()
                .get());

        Cluster.DensityStats stats = cluster.getPrimaryDensityStats();
        lw.elements = stats.getElements();

        lw.clusterSum = stats.getSum();
        lw.clusterMax = stats.getMax();
        lw.clusterStddev = stats.getStddev();
        lw.clusterMean = stats.getMean();

        lw.mean = stats.getFMean();
        lw.stddev = stats.getFStddev();
        lw.min = stats.getFMin();
        lw.max = stats.getFMax();

        lw.clusterDensityPercent = (stats.getSum() / (stats.getElements() * stats.getMax())) * (stats.getStddev() / stats.getMean());

        return lw;
    }

}
