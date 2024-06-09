# Platform model interfaces

This project contains interfaces required to follow (be implemented) for artifacts used within the platform.

They are related to three groups of artifacts:

- mining
- algorithm
- data
    - strategy
    - params
    - dataset
    - results

Samples of usage are available in [`../algorithms/samples/`](../algorithms/samples/)

## Mining

Used for main classes to define type of the data mining method such as classification or clustering.
Sample in algorithms impl.

## Algorithm

Used to define algorithm config with its pipeline
through [AlgorithmConfig.java](./src/main/java/pl/edu/pw/ddm/platform/interfaces/algorithm/AlgorithmConfig.java)
interface

Samples
in [`../algorithms/samples/random-classifier/`](../algorithms/samples/random-classifier/), [`../algorithms/samples/k-means-weka/`](../algorithms/samples/k-means-weka/), [`../algorithms/samples/svm-weka/`](../algorithms/samples/svm-weka/)

## Data

Divides on four groups of interfaces:

- First,
  introduces [interface](./src/main/java/pl/edu/pw/ddm/platform/interfaces/data/strategy/PartitionerStrategy.java) for
  custom partitioning strategy implementation
    - sample
      in [`../algorithms/samples/dense-and-outliers-strategy/`](../algorithms/samples/dense-and-outliers-strategy/)
- Second, introduces [params](./src/main/java/pl/edu/pw/ddm/platform/interfaces/data/ParamProvider.java) providing
  abstraction of [distance function](./src/main/java/pl/edu/pw/ddm/platform/interfaces/data/DistanceFunction.java) and
  parameters access in algorithm impl.
    - sample in [`../algorithms/samples/equality-distance/`](../algorithms/samples/equality-distance/) and algorithms
      impl.
- Third, introduces [dataset](./src/main/java/pl/edu/pw/ddm/platform/interfaces/data/DataProvider.java) providing
  abstraction of dataset access in algorithm impl.
    - sample in algorithms impl.
- Fourth, introduces [results](./src/main/java/pl/edu/pw/ddm/platform/interfaces/data/DataProvider.java) providing
  abstraction of results collector used for gathering results of the algorithms executions
