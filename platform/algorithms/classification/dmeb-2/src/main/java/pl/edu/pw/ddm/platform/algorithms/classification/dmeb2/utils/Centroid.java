package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils;

import java.io.Serializable;
import java.util.Arrays;

public class Centroid implements Serializable {
    private final double[] features;

    public Centroid(double[] features) {
        this.features = features;
    }

    public double[] getFeatures() {
        return features;
    }

    @Override
    public String toString() {
        return "Centroid{" +
                "features=" + Arrays.toString(features) +
                '}';
    }
}
