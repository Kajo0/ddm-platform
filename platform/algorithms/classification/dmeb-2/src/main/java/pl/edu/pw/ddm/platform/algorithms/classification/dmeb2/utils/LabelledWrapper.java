package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabelledWrapper extends LabeledObservation {

    private int elements;
    private double[] mean;
    private double[] stddev;
    private double[] min;
    private double[] max;

    public LabelledWrapper(LabeledObservation observation) {
        super(observation.getIndex(), observation.getFeatures(), observation.getTarget());
    }

    public static LabelledWrapper ofPrecalculated(MEBCluster cluster) {
        LabelledWrapper lw = new LabelledWrapper(cluster.getClusterElementList()
                .stream()
                .findFirst()
                .get());

        lw.elements = cluster.getPrimaryDensityStats().getElements();
        lw.mean = cluster.getPrimaryDensityStats().getFMean();
        lw.stddev = cluster.getPrimaryDensityStats().getFStddev();
        lw.min = cluster.getPrimaryDensityStats().getFMin();
        lw.max = cluster.getPrimaryDensityStats().getFMax();

        return lw;
    }

}
