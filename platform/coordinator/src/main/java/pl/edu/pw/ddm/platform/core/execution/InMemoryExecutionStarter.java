package pl.edu.pw.ddm.platform.core.execution;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pl.edu.pw.ddm.platform.core.instance.InstanceAgentAddressFactory;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

@Slf4j
@Service
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class InMemoryExecutionStarter implements ExecutionStarter {

    private final RestTemplate restTemplate;

    private final Map<String, ExecutionStarter.ExecutionDesc> executionMap = new HashMap<>();

    @Override
    public String start(InstanceAddrDto masterAddr, String instanceId, String algorithmId, String dataId) {
        log.info("Starting algorithm with id '{}' and data with id '{}' on master node '{}'.", algorithmId, dataId, masterAddr);

        String url = InstanceAgentAddressFactory.startExecution(masterAddr, instanceId, algorithmId, dataId);
        String executionId = restTemplate.getForObject(url, String.class);
        log.debug("Execution start data executionId: '{}'.", executionId);

        ExecutionDesc desc = ExecutionDesc.builder()
                .id(executionId)
                .instanceId(instanceId)
                .algorithmId(algorithmId)
                .dataId(dataId)
                .masterAddr(masterAddr)
                .status(ExecutionDesc.ExecutionStatus.STARTED)
                .build();
        executionMap.put(executionId, desc);

        return executionId;
    }

    @Override
    public String stop(String executionId) {
        return "TODO - not implemented stop: " + executionId;
    }

    @Override
    public ExecutionDesc status(String executionId) {
        // TODO call master for status update
        return executionMap.get(executionId);
    }

    @Override
    public Map<String, ExecutionDesc> allExecutionsInfo() {
        return executionMap;
    }

}
