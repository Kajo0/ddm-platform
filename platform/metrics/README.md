# Quality metrics

This project contains quality metrics implementations that are embedded in the Coordinator app

## List of metrics

| type           | measure                           | codename    | implementation                                                                                                      |
|----------------|-----------------------------------|-------------|---------------------------------------------------------------------------------------------------------------------|
| Clustering     | adjusted Rand index (ARI)         | `ARI`       | [DdmAdjustedRandIndex.java](src/main/java/pl/edu/pw/ddm/platform/metrics/DdmAdjustedRandIndex.java)                 |
| Clustering     | adjusted mutual information (AMI) | `AMI`       | [DdmAdjustedMutualInformation.java](src/main/java/pl/edu/pw/ddm/platform/metrics/DdmAdjustedMutualInformation.java) |
| Classification | accuracy                          | `accuracy`  | [DdmAccuracy.java](src/main/java/pl/edu/pw/ddm/platform/metrics/DdmAccuracy.java)                                   |
| Classification | precision                         | `precision` | [DdmPrecision.java](src/main/java/pl/edu/pw/ddm/platform/metrics/DdmPrecision.java)                                 |
| Classification | recall                            | `recall`    | [DdmRecall.java](src/main/java/pl/edu/pw/ddm/platform/metrics/DdmRecall.java)                                       |
| Classification | F1-score                          | `f-measure` | [DdmFMeasure.java](src/main/java/pl/edu/pw/ddm/platform/metrics/DdmFMeasure.java)                                   |
