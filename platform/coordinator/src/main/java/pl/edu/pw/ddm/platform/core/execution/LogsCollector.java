package pl.edu.pw.ddm.platform.core.execution;

import java.util.List;

import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

interface LogsCollector {

    String collectAll(List<InstanceAddrDto> addresses, ExecutionStarter.ExecutionDesc desc);

    String fetchSince(String executionId, String nodeId, Integer since);

    String fetchLast(String executionId, String nodeId, Integer last);

}
