package pl.edu.pw.ddm.platform.core.coordinator;

import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.ddm.platform.core.results.ValidationResultsFacade;

@RestController
@RequestMapping("coordinator/command/results")
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class ResultsCommandController {

    // TODO save already calculated results

    private final ValidationResultsFacade resultsFacade;

    // TODO create custom model dto
    @PostMapping("validate/{executionId}")
    Map<String, Double> validate(@PathVariable String executionId,
                                 @RequestParam("metrics") String metrics) {
        ValidationResultsFacade.ValidateRequest req = ValidationResultsFacade.ValidateRequest.builder()
                .executionId(executionId)
                .metrics(metrics.split(","))
                .build();

        return resultsFacade.validate(req)
                .getMetrics();
    }

}
