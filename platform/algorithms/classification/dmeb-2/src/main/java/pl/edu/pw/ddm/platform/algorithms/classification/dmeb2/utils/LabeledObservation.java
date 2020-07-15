package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils;

import static java.util.Arrays.stream;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class LabeledObservation implements Serializable {
    private final int index;
    private final double[] features;
    private final int target;


    public LabeledObservation(int index, double[] features, int target) {
        this.index = index;
        this.features = features;
        this.target = target;
    }

    public LabeledObservation(int index, int[] coordinates, int target) {
        this.index = index;
        this.features = Arrays.stream(coordinates).mapToDouble(__ -> __).toArray();
        this.target = target;
    }

    public int getIndex() {
        return index;
    }

    public double[] getFeatures() {
        return features;
    }

    public int getTarget() {
        return target;
    }

    public UnlabeledObservation toUnlabeledObservation() {
        return new UnlabeledObservation(features);
    }

    @Override
    public String toString() {
        String featuresAsString = stream(features)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(", "));
        return featuresAsString + ", " + target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LabeledObservation that = (LabeledObservation) o;
        return target == that.target &&
                Arrays.equals(features, that.features);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(target);
        result = 31 * result + Arrays.hashCode(features);
        return result;
    }
}