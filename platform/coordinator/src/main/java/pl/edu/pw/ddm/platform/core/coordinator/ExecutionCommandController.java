package pl.edu.pw.ddm.platform.core.coordinator;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.ddm.platform.core.execution.ExecutionFacade;

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
        // TODO not implemented yet
        return "stop - not implemented yet";
    }

    @GetMapping("status/executionId}")
    String status(@PathVariable String executionId) {
        // TODO not implemented yet
        return "status - not implemented yet";
    }

}
