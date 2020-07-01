package pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils;

import java.io.Serializable;
import java.util.List;

public interface SVMModel extends Serializable {
    int classify(double[] features);

    List<LabeledObservation> getSVs();
}
