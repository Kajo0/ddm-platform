package pl.edu.pw.ddm.platform.agent.execution;

public interface ExecutionLogsProvider {

    String loadAll(String executionId, String appId);

}
