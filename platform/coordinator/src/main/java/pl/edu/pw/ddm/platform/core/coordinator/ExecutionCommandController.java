package pl.edu.pw.ddm.platform.core.coordinator;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.ddm.platform.core.execution.ExecutionFacade;
import pl.edu.pw.ddm.platform.core.execution.ExecutionResultsFacade;
import pl.edu.pw.ddm.platform.core.execution.dto.ExecutionDescDto;

@RestController
@RequestMapping("coordinator/command/execution")
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class ExecutionCommandController {

    private final ExecutionFacade executionFacade;
    private final ExecutionResultsFacade executionResultsFacade;

    @PostMapping("start/{instanceId}/{algorithmId}/{trainDataId}")
    String start(@PathVariable String instanceId,
                 @PathVariable String algorithmId,
                 @PathVariable String trainDataId,
                 @RequestParam(value = "testDataId", required = false) String testDataId,
                 @RequestParam("executionParams") String executionParamsJson) {
        ExecutionFacade.StartRequest req = ExecutionFacade.StartRequest.builder()
                .instanceId(instanceId)
                .algorithmId(algorithmId)
                .trainDataId(trainDataId)
                .testDataId(testDataId)
                .executionParams(mapFromJsonString(executionParamsJson))
                .build();
        return executionFacade.start(req);
    }

    @GetMapping("stop/{executionId}")
    String stop(@PathVariable String executionId) {
        return Optional.of(executionId)
                .map(ExecutionFacade.StopRequest::of)
                .map(executionFacade::stop)
                .get();
    }

    @GetMapping("status/{executionId}")
    ExecutionDescDto status(@PathVariable String executionId) {
        return Optional.of(executionId)
                .map(ExecutionFacade.StatusRequest::of)
                .map(executionFacade::status)
                .get();
    }

    @GetMapping("results/collect/{executionId}")
    String collectResults(@PathVariable String executionId) {
        return Optional.of(executionId)
                .map(ExecutionResultsFacade.CollectResultsRequest::of)
                .map(executionResultsFacade::collectResults)
                .get();
    }

    @GetMapping("logs/collect/{executionId}")
    String collectLogs(@PathVariable String executionId) {
        return Optional.of(executionId)
                .map(ExecutionResultsFacade.CollectLogsRequest::of)
                .map(executionResultsFacade::collectLogs)
                .get();
    }

    @GetMapping(value = "info", produces = MediaType.APPLICATION_JSON_VALUE)
    String executedInfo() {
        return executionFacade.info();
    }

    @SneakyThrows
    private Map<String, String> mapFromJsonString(String executionParamsJson) {
        if (StringUtils.isBlank(executionParamsJson)) {
            return Collections.emptyMap();
        } else {
            return new ObjectMapper().readValue(executionParamsJson, new TypeReference<>() {
            });
        }
    }

}
