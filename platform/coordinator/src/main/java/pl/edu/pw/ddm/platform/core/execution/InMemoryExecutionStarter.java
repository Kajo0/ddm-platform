package pl.edu.pw.ddm.platform.core.execution;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
    public String start(InstanceAddrDto masterAddr, String instanceId, String algorithmId, String dataId, String distanceFunctionId, String distanceFunctionName, Map<String, String> executionParams) {
        log.info("Starting algorithm with id '{}' and data with id '{}' and distance function id '{}' and name '{}' on master node '{}'. (execution params: '{}')", algorithmId, dataId, distanceFunctionId, distanceFunctionName, masterAddr, executionParams);

        // TODO broadcast algorithm if not present there
        // TODO broadcast distance function if not present there

        String url = InstanceAgentAddressFactory.startExecution(masterAddr, instanceId, algorithmId, dataId);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("distanceFunctionName", distanceFunctionName);
        body.add("distanceFunctionPackageName", distanceFunctionName);
        body.add("executionParams", toJsonParams(executionParams));
        if (distanceFunctionId != null) {
            body.add("distanceFunctionId", distanceFunctionId);
        }

        String executionId = restTemplate.postForObject(url, body, String.class);
        log.debug("Execution start data executionId: '{}'.", executionId);

        ExecutionDesc desc = ExecutionDesc.builder()
                .id(executionId)
                .instanceId(instanceId)
                .algorithmId(algorithmId)
                .dataId(dataId)
                .distanceFunctionId(distanceFunctionId)
                .distanceFunctionName(distanceFunctionName)
                .masterAddr(masterAddr)
                .status(ExecutionDesc.ExecutionStatus.STARTED)
                .started(LocalDateTime.now())
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

    @SneakyThrows
    private String toJsonParams(Map<String, String> params) {
        return new ObjectMapper().writeValueAsString(params);
    }

}
