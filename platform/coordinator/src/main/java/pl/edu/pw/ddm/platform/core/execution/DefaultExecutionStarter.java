package pl.edu.pw.ddm.platform.core.execution;

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
class DefaultExecutionStarter implements ExecutionStarter {

    private final RestTemplate restTemplate;

    // TODO keep some map with executions or used nodes

    @Override
    public String start(InstanceAddrDto masterAddr, String instanceId, String algorithmId, String dataId) {
        log.info("Starting algorithm with id '{}' and data with id '{}' on master node '{}'.", algorithmId, dataId, masterAddr);

        String url = InstanceAgentAddressFactory.startExecution(masterAddr, instanceId, algorithmId, dataId);
        String executionId = restTemplate.getForObject(url, String.class);
        log.debug("Execution start data executionId: '{}'.", executionId);

        return executionId;
    }

}
