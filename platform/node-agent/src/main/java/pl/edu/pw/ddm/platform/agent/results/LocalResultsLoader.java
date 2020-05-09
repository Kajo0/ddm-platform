package pl.edu.pw.ddm.platform.agent.results;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class LocalResultsLoader implements ResultsLoader {

    // TODO properties
    private static final String EXECUTION_DIR = "/ddm/execution";
    private final static String RESULTS_FILE = "results.txt";
    private final static String STATS_FILE = "stats.json";

    @Override
    public File load(String executionId) {
        log.info("Loading results file for execution id '{}'.", executionId);
        Path path = Paths.get(LocalResultsLoader.EXECUTION_DIR, executionId, RESULTS_FILE);
        if (Files.exists(path)) {
            return path.toFile();
        } else {
            log.warn("Results for execution id '{}' not exists.", executionId);
            return null;
        }
    }

    @SneakyThrows
    @Override
    public String loadJsonStats(String executionId) {
        log.info("Loading results stats for execution id '{}'.", executionId);
        Path path = Paths.get(LocalResultsLoader.EXECUTION_DIR, executionId, STATS_FILE);
        if (Files.exists(path)) {
            return new String(Files.readAllBytes(path));
        } else {
            log.warn("Results stats for execution id '{}' not exists.", executionId);
            return null;
        }
    }

}
