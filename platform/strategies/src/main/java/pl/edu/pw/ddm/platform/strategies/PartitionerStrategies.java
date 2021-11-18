package pl.edu.pw.ddm.platform.strategies;

import java.util.Set;

public interface PartitionerStrategies {

    Set<String> PREDEFINED_STRATEGIES = Set.of(PartitionerStrategies.UNIFORM,
            PartitionerStrategies.SEPARATE_LABELS,
            PartitionerStrategies.MOST_OF_ONE_PLUS_SOME,
            PartitionerStrategies.UNBALANCEDNESS,
            PartitionerStrategies.COVARIATE_SHIFT,
            PartitionerStrategies.CONCEPT_DRIFT,
            PartitionerStrategies.CONCEPT_SHIFT);

    String UNIFORM = "uniform";
    String SEPARATE_LABELS = "separate-labels";
    String MOST_OF_ONE_PLUS_SOME = "most-of-one-plus-some";
    String UNBALANCEDNESS = "unbalancedness";
    String COVARIATE_SHIFT = "covariate-shift";
    String CONCEPT_DRIFT = "concept-drift";
    String CONCEPT_SHIFT = "concept-shift";

}
