package pl.edu.pw.ddm.platform.agent.results;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class LocalResultsLoader implements ResultsLoader {

    // TODO properties
    private static final String RESULTS_DIR = "/ddm/execution";
    private final static String RESULTS_FILE = "results.txt";

    @Override
    public File load(String executionId) {
        log.info("Loading results file for execution id '{}'.", executionId);
        Path path = Paths.get(RESULTS_DIR, executionId, RESULTS_FILE);
        if (Files.exists(path)) {
            return path.toFile();
        } else {
            log.warn("Results for execution id '{}' not exists.", executionId);
            return null;
        }
    }

}
