package pl.edu.pw.ddm.platform.core.execution;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public String start(InstanceAddrDto masterAddr, String instanceId, String algorithmId, String trainDataId, String testDataId, String distanceFunctionId, String distanceFunctionName, Integer cpuCores, Integer memoryInGb, Map<String, String> executionParams) {
        log.info("Starting algorithm with id '{}' train data with id '{}' test data with id '{}' distance function id '{}' and name '{}' with '{}' cpus and '{}'gb memory on master node '{}'. (execution params: '{}')", algorithmId, trainDataId, testDataId, distanceFunctionId, distanceFunctionName, cpuCores, memoryInGb, masterAddr, executionParams);

        // TODO broadcast algorithm if not present there
        // TODO broadcast distance function if not present there

        String jsonParams = toJsonParams(executionParams);
        String url = InstanceAgentAddressFactory.startExecution(masterAddr, instanceId, algorithmId, trainDataId);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("distanceFunctionName", distanceFunctionName);
        body.add("distanceFunctionPackageName", distanceFunctionName);
        body.add("executionParams", jsonParams);
        body.add("cpuCores", String.valueOf(cpuCores));
        body.add("memoryInGb", String.valueOf(memoryInGb));
        if (distanceFunctionId != null) {
            body.add("distanceFunctionId", distanceFunctionId);
        }
        if (testDataId != null) {
            body.add("testDataId", testDataId);
        }

        String executionId = restTemplate.postForObject(url, body, String.class);
        log.debug("Execution start data executionId: '{}'.", executionId);

        ExecutionDesc desc = ExecutionDesc.builder()
                .id(executionId)
                .instanceId(instanceId)
                .algorithmId(algorithmId)
                .trainDataId(trainDataId)
                .testDataId(testDataId)
                .distanceFunctionId(distanceFunctionId)
                .distanceFunctionName(distanceFunctionName)
                .masterAddr(masterAddr)
                .status(ExecutionDesc.ExecutionStatus.INITIALIZING)
                .started(LocalDateTime.now())
                .executionParams(jsonParams)
                .build();
        executionMap.put(executionId, desc);

        return executionId;
    }

    @Override
    public String stop(String executionId) {
        log.info("Stopping execution with id '{}'", executionId);

        // TODO add null check
        ExecutionDesc currentStatus = executionMap.get(executionId);
        // TODO use appId in the future
        String appId = currentStatus.getAppId() != null ? currentStatus.getAppId() : "dummy";

        // TODO check status before request - it may be not necessary

        String url = InstanceAgentAddressFactory.stopExecution(currentStatus.getMasterAddr(), executionId, appId);
        String response = restTemplate.postForObject(url, null, String.class);
        log.debug("Execution stop data response: '{}'.", response);

        return response;
    }

    @Override
    public ExecutionDesc status(String executionId) {
        // TODO add null check
        ExecutionDesc currentStatus = executionMap.get(executionId);

        if (currentStatus.isRunning()) {
            log.info("Execution '{}' is running so fetching update.", executionId);
            ExecutionAgentStatus update = requestForStatus(currentStatus);
            return updateStatus(currentStatus, update);
        } else {
            log.info("Execution '{}' is finished with status '{}'.", executionId, currentStatus.getStatus().getCode());
            return currentStatus;
        }
    }

    @Override
    public Map<String, ExecutionDesc> allExecutionsInfo() {
        return executionMap;
    }

    @SneakyThrows
    private String toJsonParams(Map<String, String> params) {
        return new ObjectMapper().writeValueAsString(params);
    }

    private ExecutionAgentStatus requestForStatus(ExecutionDesc currentStatus) {
        String executionId = currentStatus.getId();
        InstanceAddrDto masterAddr = currentStatus.getMasterAddr();

        String url = InstanceAgentAddressFactory.executionStatus(masterAddr, executionId);
        ResponseEntity<ExecutionAgentStatus> response = restTemplate.getForEntity(url, ExecutionAgentStatus.class);
        if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new IllegalStateException("Execution status not found for execution id " + executionId + " and address: " + masterAddr);
        }

        ExecutionAgentStatus body = response.getBody();
        log.debug("Collected execution status response: '{}'.", body);
        return body;
    }

    private ExecutionDesc updateStatus(ExecutionDesc currentExec, ExecutionAgentStatus update) {
        ExecutionDesc.ExecutionDescBuilder statusBuilder = currentExec.toBuilder();
        if (currentExec.getAppId() == null) {
            log.info("Execution '{}' assigned app id: '{}'.", currentExec.getId(), update.getAppId());
            statusBuilder.appId(update.getAppId());
        }

        if ("ERROR".equals(update.getStage())) {
            log.info("Execution '{}' status changed to 'ERROR' with message '{}'.", currentExec.getId(), update.getMessage());
            statusBuilder.status(ExecutionDesc.ExecutionStatus.FAILED)
                    .message(update.getMessage())
                    .stopped(update.getLastUpdate());
        } else if ("STOPPED".equals(update.getStage())) {
            log.info("Execution '{}' status changed to 'STOPPED'.", currentExec.getId());
            statusBuilder.status(ExecutionDesc.ExecutionStatus.STOPPED)
                    .message(update.getMessage())
                    .stopped(update.getLastUpdate());
        } else if ("FINISHED".equals(update.getStage())) {
            log.info("Execution '{}' status changed to 'FINISHED'.", currentExec.getId());
            statusBuilder.status(ExecutionDesc.ExecutionStatus.FINISHED)
                    .stopped(update.getLastUpdate());
        } else {
            log.info("Execution '{}' status changed to '{}' with message '{}'.", currentExec.getId(), update.getStage(), update.getMessage());
            statusBuilder.status(ExecutionDesc.ExecutionStatus.STARTED)
                    .message(update.getMessage())
                    .updated(update.getLastUpdate());
        }

        ExecutionDesc nextStatus = statusBuilder.build();
        executionMap.put(currentExec.getId(), nextStatus);
        return nextStatus;
    }

}
