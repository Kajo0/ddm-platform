package pl.edu.pw.ddm.platform.agent.execution;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class LocalExecutionStatusProvider implements ExecutionStatusProvider {

    @Value("${paths.execution.path}")
    private String executionPath;

    @Value("${paths.execution.status-filename}")
    private String statusFilename;

    @SneakyThrows
    @Override
    public String loadJsonStatus(String executionId) {
        log.info("Loading execution status for execution id '{}'.", executionId);
        Path path = Paths.get(executionPath, executionId, statusFilename);
        if (Files.exists(path)) {
            return new String(Files.readAllBytes(path));
        } else {
            log.warn("Result status for execution id '{}' not exists.", executionId);
            return null;
        }
    }

}
