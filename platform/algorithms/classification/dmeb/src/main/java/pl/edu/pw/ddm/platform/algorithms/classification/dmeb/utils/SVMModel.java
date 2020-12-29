package pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils;

import java.io.Serializable;
import java.util.Set;

public interface SVMModel extends Serializable {
    int classify(double[] features);

    Set<LabeledObservation> getSVs();
}
