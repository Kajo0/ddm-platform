package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils;

import java.io.Serializable;
import java.util.List;

public interface SVMModel extends Serializable {

    boolean isSingleClass();

    int classify(double[] features);

    List<LabeledObservation> getSVs();
}
