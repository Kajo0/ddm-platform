package pl.edu.pw.ddm.platform.agent.api;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.ddm.platform.agent.healthcheck.HealthCheckService;

@RestController
@RequestMapping("agent/health-check")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class HealthCheckController {

    private final HealthCheckService healthCheckService;

    @GetMapping("status")
    ResponseEntity<Void> status() {
        if (healthCheckService.ready()) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

}
