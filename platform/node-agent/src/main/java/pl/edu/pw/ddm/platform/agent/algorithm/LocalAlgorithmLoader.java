package pl.edu.pw.ddm.platform.agent.algorithm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class LocalAlgorithmLoader implements AlgorithmLoader {

    // TODO properties
    private static final String ALGORITHMS_PATH = "/ddm/algorithms";

    LocalAlgorithmLoader() throws IOException {
        Files.createDirectories(Paths.get(ALGORITHMS_PATH));
    }

    @SneakyThrows
    @Override
    public boolean save(byte[] bytes, String algorithmId) {
        Path path = Paths.get(ALGORITHMS_PATH, algorithmId);
        Files.createDirectories(path.getParent());

        boolean exist = Files.deleteIfExists(path);
        if (exist) {
            log.info("Previous algorithm file deleted.");
        }

        log.info("Saving algorithm file with id '{}'.", algorithmId);
        Files.write(path, bytes);

        return exist;
    }

    @Override
    public File load(String algorithmId) {
        log.info("Loading algorithm file with id '{}'.", algorithmId);
        return Paths.get(ALGORITHMS_PATH, algorithmId).toFile();
    }

}
