package pl.edu.pw.ddm.platform.strategies;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public interface PartitionerStrategies {

    Set<String> PREDEFINED_STRATEGIES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            PartitionerStrategies.UNIFORM,
            PartitionerStrategies.SEPARATE_LABELS,
            PartitionerStrategies.MOST_OF_ONE_PLUS_SOME,
            PartitionerStrategies.UNBALANCEDNESS,
            PartitionerStrategies.COVARIATE_SHIFT
    )));

    String UNIFORM = "uniform";
    String SEPARATE_LABELS = "separate-labels";
    String MOST_OF_ONE_PLUS_SOME = "most-of-one-plus-some";
    String UNBALANCEDNESS = "unbalancedness";
    String COVARIATE_SHIFT = "covariate-shift";

}
