package pl.edu.pw.ddm.platform.agent.execution;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import pl.edu.pw.ddm.platform.agent.util.ProfileConstants;

@Slf4j
@Service
@Profile(ProfileConstants.MASTER)
class LocalMasterExecutionLogsProvider implements ExecutionLogsProvider {

    @Value("${paths.execution.path}")
    private String executionPath;

    @Value("${paths.execution.logs.central-filename}")
    private String centralLogFilename;

    @SneakyThrows
    @Override
    public String loadAll(String executionId, String appId) {
        log.info("Loading execution logs for execution id '{}' and app id '{}'.", executionId, appId);
        Path path = Paths.get(executionPath, executionId, centralLogFilename);
        if (Files.exists(path)) {
            return Files.readAllLines(path)
                    .stream()
                    .filter(LocalWorkerExecutionLogsProvider.SPARK_LOG_FILTER_PREDICATE)
                    .collect(Collectors.joining(LocalWorkerExecutionLogsProvider.LINE_END));
        } else {
            log.warn("Log file not found for execution id '{}' and app id '{}'.", executionId, appId);
            return null;
        }
    }

}
