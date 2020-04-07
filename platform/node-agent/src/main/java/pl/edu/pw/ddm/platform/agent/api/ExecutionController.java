package pl.edu.pw.ddm.platform.agent.api;

import java.io.IOException;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.ddm.platform.agent.runner.AppRunner;

@RestController
@RequestMapping("agent/execution")
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class ExecutionController {

    private final AppRunner appRunner;

    @GetMapping("run/{instanceId}/{algorithmId}/{dataId}")
    String run(@PathVariable String instanceId, @PathVariable String algorithmId, @PathVariable String dataId) {
        if (appRunner.isProgramRunning()) {
            // TODO change response
            throw new IllegalStateException("Another process is already running");
        }

        try {
            String executionId = appRunner.run(instanceId, algorithmId, dataId);
            return executionId;
        } catch (IOException e) {
            e.printStackTrace();
            return "IOException: " + e.getMessage();
        }
    }

    @GetMapping("stop/{executionId}")
    String stop(@PathVariable String executionId) {
        return "not implemented yet - stop execution of " + executionId;
    }

}
