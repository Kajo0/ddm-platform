# App runner for java execution

This project is responsible for algorithm execution in the workers run.
The app runner and collect all statistics related to the execution/load/training etc. times and transfer load which is
hold in [CentralDdmSummarizer.java](./src/main/java/pl/edu/pw/ddm/platform/runner/utils/CentralDdmSummarizer.java)
during execution

Current implementation in [CentralRunner.java](./src/main/java/pl/edu/pw/ddm/platform/runner/CentralRunner.java) uses
Spark application for execution - mostly for code distribution and orchestration.
