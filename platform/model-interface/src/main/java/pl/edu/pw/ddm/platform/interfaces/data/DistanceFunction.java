package pl.edu.pw.ddm.platform.interfaces.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public interface DistanceFunction {

    Set<String> PREDEFINED_FUNCTIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            PredefinedNames.EUCLIDEAN,
            PredefinedNames.COSINE,
            PredefinedNames.NONE
    )));

    interface PredefinedNames {
        String EUCLIDEAN = "euclidean";
        String COSINE = "cosine";
        String NONE = "none";
    }

    String name();

    double distance(Data first, Data second);

    double distance(double[] first, double[] second);

    double distance(String[] first, String[] second);

}
