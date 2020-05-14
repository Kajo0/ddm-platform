package pl.edu.pw.ddm.platform.core.algorithm;

import javax.annotation.PostConstruct;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.edu.pw.ddm.platform.core.util.IdGenerator;

@Slf4j
@Service
class LocalAlgorithmLoader implements AlgorithmLoader {

    @Value("${paths.algorithms}")
    private String algorithmsPath;

    private final Map<String, AlgorithmDesc> algorithmMap = new HashMap<>();

    @SneakyThrows
    @Override
    public String save(String name, byte[] jar) {
        var id = IdGenerator.generate(name + "/" + jar.length);

        String ext = FilenameUtils.getExtension(name);
        Path algPath = Paths.get(algorithmsPath, id + "." + ext);
        Files.deleteIfExists(algPath);
        Files.write(algPath, jar);

        try {
            var typePackage = new ProcessorPackageEvaluator()
                    .callForPackageName(algPath.toFile());
            var alg = new AlgorithmDesc(id, name, typePackage.getPackageName(), typePackage.getAlgorithmType(), typePackage.getAlgorithmName(), (long) jar.length, algPath.toString());

            if (algorithmMap.putIfAbsent(id, alg) != null) {
                log.warn("Loaded the same jar '{}' file as before with id '{}'.", name, id);
            }
            return id;
        } catch (Exception e) {
            log.error("Error during algorithm package name evaluation so removing saved jar.", e);
            Files.delete(algPath);
            throw e;
        }
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

    @PostConstruct
    void init() throws IOException {
        // TODO save on PreDestroy and collect or keep removed
        Files.createDirectories(Paths.get(algorithmsPath));
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
