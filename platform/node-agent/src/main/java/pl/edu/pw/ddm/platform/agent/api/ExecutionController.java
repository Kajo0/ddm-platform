package pl.edu.pw.ddm.platform.agent.api;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.ddm.platform.agent.execution.ExecutionLogsProvider;
import pl.edu.pw.ddm.platform.agent.execution.ExecutionStatusProvider;
import pl.edu.pw.ddm.platform.agent.runner.AppRunner;

@RestController
@RequestMapping("agent/execution")
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class ExecutionController {

    private final AppRunner appRunner;
    private final ExecutionStatusProvider statusProvider;
    private final ExecutionLogsProvider logsProvider;

    // TODO think about request params and post dto due to another parameters
    @PostMapping("run/{instanceId}/{algorithmId}/{trainDataId}")
    String run(@PathVariable("instanceId") String instanceId,
               @PathVariable("algorithmId") String algorithmId,
               @PathVariable("trainDataId") String trainDataId,
               @RequestParam(value = "testDataId", required = false) String testDataId,
               @RequestParam(value = "distanceFunctionId", required = false) String distanceFunctionId,
               @RequestParam("distanceFunctionName") String distanceFunctionName,
               @RequestParam("cpuCores") Integer cpuCores,
               @RequestParam("memoryInGb") Integer memoryInGb,
               @RequestParam("executionParams") String executionParamsJson) {
        if (appRunner.isProgramRunning()) {
            // TODO change response
            throw new IllegalStateException("Another process is already running");
        }

        try {
            // TODO move to request as body for auto populate
            AppRunner.AppRunnerParamsDto params = AppRunner.AppRunnerParamsDto.builder()
                    .instanceId(instanceId)
                    .algorithmId(algorithmId)
                    .trainDataId(trainDataId)
                    .testDataId(testDataId)
                    .distanceFunctionId(Strings.emptyToNull(distanceFunctionId))
                    .distanceFunctionName(distanceFunctionName)
                    .cpuCount(cpuCores)
                    .memoryInGb(memoryInGb)
                    .executionParams(mapFromJsonString(executionParamsJson))
                    .build();

            return appRunner.run(params);
        } catch (IOException e) {
            // TODO think about return messages
            e.printStackTrace();
            return "IOException: " + e.getMessage();
        }
    }

    @GetMapping("stop/{executionId}")
    String stop(@PathVariable String executionId) {
        return "not implemented yet - stop execution of " + executionId;
    }

    // TODO define model
    @GetMapping(value = "status/{executionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> status(@PathVariable String executionId) {
        String status = statusProvider.loadJsonStatus(executionId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(status);
        }
    }

    @GetMapping(value = "logs/{executionId}/{appId}", produces = MediaType.TEXT_PLAIN_VALUE)
    ResponseEntity<String> logs(@PathVariable String executionId, @PathVariable String appId) {
        String logs = logsProvider.loadAll(executionId, appId);
        if (logs == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(logs);
        }
    }

    @SneakyThrows
    private Map<String, String> mapFromJsonString(String executionParamsJson) {
        return new ObjectMapper().readValue(executionParamsJson, new TypeReference<Map<String, String>>() {
        });
    }

}
