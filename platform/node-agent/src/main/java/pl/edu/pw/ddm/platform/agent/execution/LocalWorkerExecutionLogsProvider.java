package pl.edu.pw.ddm.platform.agent.execution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import pl.edu.pw.ddm.platform.agent.util.ProfileConstants;

@Slf4j
@Service
@Profile(ProfileConstants.WORKER)
class LocalWorkerExecutionLogsProvider implements ExecutionLogsProvider {

    private static final String SPARK_LOG_DIR = "/spark/work";

    @SneakyThrows
    @Override
    public String loadAll(String executionId, String appId) {
        log.info("Loading execution logs for execution id '{}' and app id '{}'.", executionId, appId);
        Path appPath = Paths.get(SPARK_LOG_DIR, appId);
        if (Files.exists(appPath)) {
            // FIXME add some speicif logger or load via spark node API
            return Files.find(appPath, 10, (filePath, fileAttr) -> fileAttr.isRegularFile() && "stdout".equals(filePath.toFile().getName()))
                    .map(path -> {
                        try {
                            return Files.readAllBytes(path);
                        } catch (IOException e) {
                            return e.getMessage().getBytes();
                        }
                    })
                    .map(String::new)
                    .collect(Collectors.joining("\n\n"));
        } else {
            log.warn("Log dir not found for execution id '{}' and app id '{}'.", executionId, appId);
            return null;
        }
    }

}
