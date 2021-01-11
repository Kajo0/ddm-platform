package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils;

import java.io.Serializable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import pl.edu.pw.ddm.platform.interfaces.data.Data;

@Getter
@ToString
@RequiredArgsConstructor
public class UnlabeledObservation implements Serializable, Data {

    private final double[] features;

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public String[] getAttributes() {
        String[] result = new String[features.length];
        for (int i = 0; i < features.length; ++i) {
            result[i] = String.valueOf(features[i]);
        }
        return result;
    }

    @Override
    public double[] getNumericAttributes() {
        return features;
    }

    @Override
    public String getAttribute(int i) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String getAttribute(String s) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public double getNumericAttribute(int i) {
        return features[i];
    }

    @Override
    public double getNumericAttribute(String s) {
        throw new RuntimeException("Not implemented");
    }

}
