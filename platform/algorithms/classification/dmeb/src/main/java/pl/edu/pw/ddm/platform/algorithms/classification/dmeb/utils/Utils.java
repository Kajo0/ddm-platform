package pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils;

import java.util.List;

public class Utils {

    public static boolean moreThanOneClass(List<LabeledObservation> trainingSet) {
        return trainingSet.stream()
                .map(LabeledObservation::getTarget)
                .distinct().count() > 1;
    }

}
