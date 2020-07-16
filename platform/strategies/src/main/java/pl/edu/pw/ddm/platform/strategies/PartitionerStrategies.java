package pl.edu.pw.ddm.platform.strategies;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public interface PartitionerStrategies {

    Set<String> PREDEFINED_STRATEGIES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            PartitionerStrategies.UNIFORM,
            PartitionerStrategies.SEPARATE_LABELS
    )));

    String UNIFORM = "uniform";
    String SEPARATE_LABELS = "separate-labels";

}
