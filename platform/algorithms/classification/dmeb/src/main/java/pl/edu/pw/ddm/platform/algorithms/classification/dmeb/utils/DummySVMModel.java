package pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils;

import java.util.Collections;
import java.util.Set;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DummySVMModel implements SVMModel {

    private final int target;

    @Override
    public int classify(double[] features) {
        return target;
    }

    @Override
    public Set<LabeledObservation> getSVs() {
        return Collections.emptySet();
    }

}
