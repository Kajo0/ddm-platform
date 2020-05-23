package pl.edu.pw.ddm.platform.agent.healthcheck;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class SparkHealthCheckService implements HealthCheckService {

    @Override
    public boolean ready() {
        // TODO check if spark master/worker is working connected etc
        log.info("Checking health (ok).");
        return true;
    }

}
