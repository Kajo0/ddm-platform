# Node Agent

This project is related to the agent application which communicates with the Coordinator app from every worker node.

In terms of the execution, current
implementation ([AppRunner.java](./src/main/java/pl/edu/pw/ddm/platform/agent/runner/AppRunner.java))
uses `CentralRunner` from the [`../app-runner-java/`](../app-runner-java/) for execution as defined
in [application.yml](./src/main/resources/application.yml) and Spark to distribute code and handle communication.

## Responsibilities

This app is responsible for:

- loading artifacts such as algorithm and datasets for execution
- controlling of the algorithms execution (start/stop)
    - with `nodemaster` profile it starts execution of the central coordinated pipeline
- providing worker healthcheck, results and logs from the execution
