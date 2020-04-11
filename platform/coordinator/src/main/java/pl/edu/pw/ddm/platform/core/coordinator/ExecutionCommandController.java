package pl.edu.pw.ddm.platform.core.coordinator;

import java.util.Optional;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.ddm.platform.core.execution.ExecutionFacade;
import pl.edu.pw.ddm.platform.core.execution.dto.ExecutionDescDto;

@RestController
@RequestMapping("coordinator/command/execution")
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class ExecutionCommandController {

    private final ExecutionFacade executionFacade;

    @GetMapping("start/{instanceId}/{algorithmId}/{dataId}")
    String start(@PathVariable String instanceId,
                 @PathVariable String algorithmId,
                 @PathVariable String dataId) {
        // TODO parameters
        ExecutionFacade.StartRequest req = ExecutionFacade.StartRequest.builder()
                .instanceId(instanceId)
                .algorithmId(algorithmId)
                .dataId(dataId)
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
                .map(ExecutionFacade.CollectResultsRequest::of)
                .map(executionFacade::collectResults)
                .get();
    }

    @GetMapping(value = "info", produces = MediaType.APPLICATION_JSON_VALUE)
    String executedInfo() {
        return executionFacade.info();
    }

}
