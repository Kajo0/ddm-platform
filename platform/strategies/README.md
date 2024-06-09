# Partitioning strategies

This project contains partitioning strategies that are embedded in the Coordinator app

## List of strategies

| non-IID main category          | codename                | implementation                                                                                                                               | parameters[^1] as example                                                                                     |
|--------------------------------|-------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|
| n/a                            | `uniform`               | [UniformPartitionerStrategy](./src/main/java/pl/edu/pw/ddm/platform/strategies/UniformPartitionerStrategy.java)                              | n/a                                                                                                           |
| Label distribution skew        | `separate-labels`       | [SeparateByLabelsPartitionerStrategy](./src/main/java/pl/edu/pw/ddm/platform/strategies/SeparateByLabelsPartitionerStrategy.java)            | n/a                                                                                                           |
| Label distribution skew        | `most-of-one-plus-some` | [MostOfOnePlusSomePartitionerStrategy](./src/main/java/pl/edu/pw/ddm/platform/strategies/mostof/MostOfOnePlusSomePartitionerStrategy.java)   | `{"additionalClassesNumber":2;"emptyWorkerFill":8;"fillEmptyButPercent":0.8;"additionalClassesPercent":0.05}` |
| Quantity skew                  | `unbalancedness`        | [UnbalancedPartitionerStrategy](./src/main/java/pl/edu/pw/ddm/platform/strategies/UnbalancedPartitionerStrategy.java)                        | `{"proportional":1;"nodeThreshold":1;"unbalancedness":0.1}`                                                   |
| Feature distribution skew      | `covariate-shift`       | [CovariateShiftPartitionerStrategy](./src/main/java/pl/edu/pw/ddm/platform/strategies/covariateshift/CovariateShiftPartitionerStrategy.java) | `{"attribute":0;"shift":0.3;"splits":3;"method":0}`                                                           |
| Same label, different features | `concept-drift`         | [ConceptDriftPartitionerStrategy](./src/main/java/pl/edu/pw/ddm/platform/strategies/conceptdrift/ConceptDriftPartitionerStrategy.java)       | `{"drifts":3;"discreteRanges":40;"label":1}`                                                                  |
| Same features, different label | `concept-shift`         | [ConceptShiftPartitionerStrategy](./src/main/java/pl/edu/pw/ddm/platform/strategies/conceptshift/ConceptShiftPartitionerStrategy.java)       | `{"shifts":2;"label":1}`                                                                                      |

[^1]: Described within ::availableParameters methods of the strategy impl.
