package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils;

import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DummySVMModel implements SVMModel {

    private final int target;

    @Override
    public boolean isSingleClass() {
        return true;
    }

    @Override
    public int classify(double[] features) {
        return target;
    }

    @Override
    public List<LabeledObservation> getSVs() {
        return Collections.emptyList();
    }

}
