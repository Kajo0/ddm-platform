package pl.edu.pw.ddm.platform.core.algorithm;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import pl.edu.pw.ddm.platform.core.util.IdGenerator;

@Slf4j
@Service
class LocalAlgorithmLoader implements AlgorithmLoader {

    // TODO move to properties
    private static final String ALGORITHMS_PATH = "/coordinator/algorithms";

    private final Map<String, AlgorithmDesc> algorithmMap = new HashMap<>();

    LocalAlgorithmLoader() throws IOException {
        // TODO save on PreDestroy and collect or keep removed
        Files.createDirectories(Paths.get(ALGORITHMS_PATH));
    }

    @SneakyThrows
    @Override
    public String save(String name, byte[] jar) {
        var id = IdGenerator.generate(name + "/" + jar.length);

        String ext = FilenameUtils.getExtension(name);
        Path algPath = Paths.get(ALGORITHMS_PATH, id + "." + ext);
        Files.deleteIfExists(algPath);
        Files.write(algPath, jar);

        // TODO extract algorithm type
        var alg = new AlgorithmDesc(id, name, "TODO", (long) jar.length, algPath.toString());
        if (algorithmMap.putIfAbsent(id, alg) != null) {
            log.warn("Loaded the same jar '{}' file as before with id '{}'.", name, id);
        }
        return id;
    }

    @Override
    public File load(String algorithmId) {
        AlgorithmDesc desc = getAlgorithm(algorithmId);
        log.debug("Loading algorithm with id '{}' from '{}'.", algorithmId, desc.getLocation());

        return Optional.of(desc.getLocation())
                .map(Path::of)
                .map(Path::toFile)
                .get();
    }

    @Override
    public AlgorithmDesc getAlgorithm(String algorithmId) {
        return algorithmMap.get(algorithmId);
    }

    // TODO remove debug feature or change to proper immutable informational
    @Override
    public Map<String, AlgorithmDesc> allAlgorithmsInfo() {
        return algorithmMap;
    }

    @PreDestroy
    void destroy() {
        // TODO disable for persistent config
        log.info("PreDestroy " + this.getClass().getSimpleName());
        algorithmMap.values()
                .parallelStream()
                .map(AlgorithmDesc::getLocation)
                .forEach(location -> {
                    try {
                        log.debug("Deleting algorithm: '{}'.", location);
                        Files.deleteIfExists(Path.of(location));
                    } catch (IOException e) {
                        log.error("Failed to remove algorithm file: '{}'.", location);
                    }
                });
    }

}
