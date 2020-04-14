package pl.edu.pw.ddm.platform.agent.api;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.ddm.platform.agent.runner.AppRunner;

@RestController
@RequestMapping("agent/execution")
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class ExecutionController {

    private final AppRunner appRunner;

    // TODO think about request params and post dto due to another parameters
    @PostMapping("run/{instanceId}/{algorithmId}/{dataId}")
    String run(@PathVariable String instanceId,
               @PathVariable String algorithmId,
               @PathVariable String dataId,
               @RequestParam(value = "distanceFunctionId", required = false) String distanceFunctionId,
               @RequestParam("distanceFunctionName") String distanceFunctionName,
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
                    .dataId(dataId)
                    .distanceFunctionId(Strings.emptyToNull(distanceFunctionId))
                    .distanceFunctionName(distanceFunctionName)
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

    @GetMapping("status/{executionId}")
    String status(@PathVariable String executionId) {
        return "not implemented yet - status execution of " + executionId;
    }

    @SneakyThrows
    private Map<String, String> mapFromJsonString(String executionParamsJson) {
        return new ObjectMapper().readValue(executionParamsJson, new TypeReference<Map<String, String>>() {
        });
    }

}
