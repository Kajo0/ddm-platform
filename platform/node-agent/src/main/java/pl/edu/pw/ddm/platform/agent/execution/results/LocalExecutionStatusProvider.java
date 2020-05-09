package pl.edu.pw.ddm.platform.agent.execution.results;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class LocalExecutionStatusProvider implements ExecutionStatusProvider {

    // TODO properties
    private static final String EXECUTION_DIR = "/ddm/execution";
    private final static String STATUS_FILE = "status.json";

    @SneakyThrows
    @Override
    public String loadJsonStatus(String executionId) {
        log.info("Loading execution status for execution id '{}'.", executionId);
        Path path = Paths.get(EXECUTION_DIR, executionId, STATUS_FILE);
        if (Files.exists(path)) {
            return new String(Files.readAllBytes(path));
        } else {
            log.warn("Result status for execution id '{}' not exists.", executionId);
            return null;
        }
    }

}
