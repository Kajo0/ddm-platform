package pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.utils;

import java.io.Serializable;
import java.util.Set;

public interface SVMModel extends Serializable {

    boolean isSingleClass();

    int classify(double[] features);

    Set<LabeledObservation> getSVs();
}
