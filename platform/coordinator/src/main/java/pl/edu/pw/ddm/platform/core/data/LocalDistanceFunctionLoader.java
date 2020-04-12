package pl.edu.pw.ddm.platform.core.data;

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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pw.ddm.platform.core.util.IdGenerator;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;

@Slf4j
@Service
class LocalDistanceFunctionLoader implements DistanceFunctionLoader {

    // TODO move to properties
    private static final String FUNC_PATH = "/coordinator/distance_functions";

    private final Map<String, DistanceFunctionDesc> distanceFunctionsMap = new HashMap<>();

    @SneakyThrows
    @Override
    public String save(MultipartFile file) {
        var id = IdGenerator.generate(file.getOriginalFilename() + file.getSize());

        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        Path funcPath = Paths.get(FUNC_PATH, id + "." + ext);
        Files.write(funcPath, file.getBytes());

        try {
            DistanceFunctionEvaluator.NamePackageDto namePackage = new DistanceFunctionEvaluator()
                    .callForFunctionName(funcPath.toFile());
            if (DistanceFunction.PREDEFINED_FUNCTIONS.contains(namePackage.getFunctionName())) {
                // TODO block it ?
                log.warn("Adding distance function with one of predefined names: '{}'.", namePackage.getFunctionName());
            }
            var func = new DistanceFunctionDesc(id, file.getOriginalFilename(), namePackage.getPackageName(), namePackage.getFunctionName(), file.getSize(), funcPath.toString());

            if (distanceFunctionsMap.put(id, func) != null) {
                log.warn("Loaded probably the same distance func as before with id '{}'.", id);
            }
            return id;
        } catch (Exception e) {
            log.error("Error during distance function name evaluation so removing saved jar.", e);
            Files.delete(funcPath);
            throw e;
        }
    }

    @Override
    public File load(String distanceFunctionId) {
        var desc = getDistanceFunctionDesc(distanceFunctionId);
        log.debug("Loading distance function with id '{}' from '{}'.", distanceFunctionId, desc.getLocation());

        return Optional.of(desc)
                .map(DistanceFunctionDesc::getLocation)
                .map(Path::of)
                .map(Path::toFile)
                .get();
    }

    @Override
    public DistanceFunctionDesc getDistanceFunctionDesc(String distanceFunctionId) {
        return distanceFunctionsMap.get(distanceFunctionId);
    }

    @Override
    public Map<String, DistanceFunctionDesc> allDistanceFunctionInfo() {
        return distanceFunctionsMap;
    }

    @PostConstruct
    void init() throws IOException {
        // TODO save on PreDestroy and collect or keep removed
        Files.createDirectories(Paths.get(FUNC_PATH));
    }

    @PreDestroy
    void destroy() {
        // TODO disable for persistent config
        log.info("PreDestroy " + this.getClass().getSimpleName());
        distanceFunctionsMap.values()
                .parallelStream()
                .map(DistanceFunctionDesc::getLocation)
                .forEach(location -> {
                    try {
                        log.debug("Deleting distance function: '{}'.", location);
                        Files.deleteIfExists(Path.of(location));
                    } catch (IOException e) {
                        log.error("Failed to remove distance function file: '{}'.", location);
                    }
                });
    }

}
