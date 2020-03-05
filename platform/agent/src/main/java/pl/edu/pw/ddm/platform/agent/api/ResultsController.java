package pl.edu.pw.ddm.platform.agent.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("agent/results")
class ResultsController {

    @GetMapping("global/{executionId}")
    String global(@PathVariable String executionId) {
        return "globalResults of " + executionId;
    }

}
