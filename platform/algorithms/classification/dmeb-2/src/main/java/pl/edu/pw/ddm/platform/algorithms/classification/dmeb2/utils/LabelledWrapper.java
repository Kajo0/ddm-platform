package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class LabelledWrapper extends LabeledObservation {

    private int elements;
    private double[] mean;
    private double[] stddev;
    private double[] min;
    private double[] max;
    private double clusterDensityPercent;

    public LabelledWrapper(LabeledObservation observation) {
        super(observation.getIndex(), observation.getFeatures(), observation.getTarget());
    }

    public static LabelledWrapper ofPrecalculated(MEBCluster cluster) {
        LabelledWrapper lw = new LabelledWrapper(cluster.getClusterElementList()
                .stream()
                .findFirst()
                .get());

        MEBCluster.DensityStats stats = cluster.getPrimaryDensityStats();
        lw.elements = stats.getElements();

        lw.mean = stats.getFMean();
        lw.stddev = stats.getFStddev();
        lw.min = stats.getFMin();
        lw.max = stats.getFMax();

        lw.clusterDensityPercent = (stats.getSum() / (stats.getElements() * stats.getMax())) * (stats.getStddev() / stats.getMean());

        return lw;
    }

}
