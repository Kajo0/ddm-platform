package pl.edu.pw.ddm.platform.agent.execution;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import pl.edu.pw.ddm.platform.agent.util.ProfileConstants;

@Slf4j
@Service
@Profile(ProfileConstants.MASTER)
class LocalMasterExecutionLogsProvider implements ExecutionLogsProvider {

    // TODO properties
    private static final String EXECUTION_DIR = "/ddm/execution";
    private final static String LOG_FILE = "central.log";

    @SneakyThrows
    @Override
    public String loadAll(String executionId, String appId) {
        log.info("Loading execution logs for execution id '{}' and app id '{}'.", executionId, appId);
        Path path = Paths.get(EXECUTION_DIR, executionId, LOG_FILE);
        if (Files.exists(path)) {
            return new String(Files.readAllBytes(path));
        } else {
            log.warn("Log file not found for execution id '{}' and app id '{}'.", executionId, appId);
            return null;
        }
    }

}
