package pl.edu.pw.ddm.platform.agent.api;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.ddm.platform.agent.runner.AppRunner;

@RestController
@RequestMapping("agent/execution")
class ExecutionController {

    @GetMapping("run/{algorithmId}/{dataId}")
    String run(@PathVariable String algorithmId, @PathVariable String dataId) {
        try {
            String executionId = AppRunner.run(algorithmId, dataId);
            return "executionId: " + executionId;
        } catch (IOException e) {
            e.printStackTrace();
            return "IOException: " + e.getMessage();
        }
    }

    @GetMapping("stop/{executionId}")
    String stop(@PathVariable String executionId) {
        return "stop execution of " + executionId;
    }

}
