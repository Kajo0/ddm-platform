package pl.edu.pw.ddm.platform.agent.execution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import pl.edu.pw.ddm.platform.agent.util.ProfileConstants;

@Slf4j
@Service
@Profile(ProfileConstants.WORKER)
class LocalWorkerExecutionLogsProvider implements ExecutionLogsProvider {

    private static final Predicate<String> SPARK_LOG_FILTER_PREDICATE = Pattern.compile("^\\d{2}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2} .+ .*$")
            .asPredicate()
            .negate();
    private static final Predicate<String> SPARK_NON_LOG_LIKE_FILTER_PREDICATE = Pattern.compile("^========================================.*$|" +
            "^$|" +
            "^Spark Executor Command.*$|" +
            "^tdown.*$|" +
            "^Using Spark's default log4j profile.*$")
            .asPredicate()
            .negate();

    @Value("${paths.execution.logs.spark-work-dir}")
    private String sparkLogDir;

    @SneakyThrows
    @Override
    public String loadAll(String executionId, String appId) {
        log.info("Loading execution logs for execution id '{}' and app id '{}'.", executionId, appId);
        Path appPath = Paths.get(sparkLogDir, appId);
        if (Files.exists(appPath)) {
            // FIXME add some speicif logger or load via spark node API
            String stdout = Files.find(appPath, 10, (filePath, fileAttr) -> fileAttr.isRegularFile() && "stdout".equals(filePath.toFile().getName()))
                    .map(path -> {
                        try {
                            return Files.readAllBytes(path);
                        } catch (IOException e) {
                            return e.getMessage().getBytes();
                        }
                    })
                    .map(String::new)
                    .collect(Collectors.joining("\n\n"));

            // TODO optimize and move to reading from one logger file
            String stderr = Files.find(appPath, 10, (filePath, fileAttr) -> fileAttr.isRegularFile() && "stderr".equals(filePath.toFile().getName()))
                    .map(path -> {
                        try {
                            return Files.readAllLines(path);
                        } catch (IOException e) {
                            return Collections.singletonList(e.getMessage());
                        }
                    })
                    .flatMap(Collection::stream)
                    .filter(SPARK_NON_LOG_LIKE_FILTER_PREDICATE)
                    .filter(SPARK_LOG_FILTER_PREDICATE)
                    .map(l -> "error log-line:  " + l)
                    .collect(Collectors.joining("\n"));

            return stdout + "\n" + stderr;
        } else {
            log.warn("Log dir not found for execution id '{}' and app id '{}'.", executionId, appId);
            return null;
        }
    }

}
