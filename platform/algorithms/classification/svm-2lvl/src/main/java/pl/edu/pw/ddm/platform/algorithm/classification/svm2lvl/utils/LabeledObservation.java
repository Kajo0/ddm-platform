package pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.utils;

import static java.util.Arrays.stream;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.pw.ddm.platform.interfaces.data.Data;

@Getter
@RequiredArgsConstructor
public class LabeledObservation implements Serializable {

    private final int index;
    private final double[] features;
    private final int target;

    public LabeledObservation(int index, int[] coordinates, int target) {
        this.index = index;
        this.features = Arrays.stream(coordinates).mapToDouble(__ -> __).toArray();
        this.target = target;
    }

    public static LabeledObservation fromUnlabelled(Data data, int label) {
        return new LabeledObservation(-1, data.getNumericAttributes(), label);
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
