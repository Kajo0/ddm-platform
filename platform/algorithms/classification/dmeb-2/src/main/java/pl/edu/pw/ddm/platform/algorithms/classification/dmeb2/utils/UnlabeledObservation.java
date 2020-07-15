package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils;

import java.io.Serializable;

public class UnlabeledObservation implements Serializable {
    private final double[] features;

    public UnlabeledObservation(double[] features) {
        this.features = features;
    }

    public double[] getFeatures() {
        return features;
    }
}
