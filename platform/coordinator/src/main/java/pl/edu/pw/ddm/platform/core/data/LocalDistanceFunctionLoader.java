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

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pw.ddm.platform.core.util.IdGenerator;
import pl.edu.pw.ddm.platform.distfunc.EuclideanDistance;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;

@Slf4j
@Service
class LocalDistanceFunctionLoader implements DistanceFunctionLoader {

    @Value("${paths.distance-functions}")
    private String distanceFunctionsPath;

    private final Map<String, DistanceFunctionDesc> distanceFunctionsMap = new HashMap<>();
    private final Map<String, DistanceFunction> distanceFunctionsImplMap = new HashMap<>();

    @SneakyThrows
    @Override
    public String save(MultipartFile file) {
        var id = IdGenerator.generate(file.getOriginalFilename() + file.getSize());

        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        Path funcPath = Paths.get(distanceFunctionsPath, id + "." + ext);
        Files.write(funcPath, file.getBytes());

        try {
            ImplementationEvaluator.NamePackageDto namePackage = new ImplementationEvaluator()
                    .callForDistanceFunctionName(funcPath.toFile());
            if (isPredefined(namePackage.getImplName())) {
                // TODO block it ?
                log.warn("Adding distance function with one of predefined names: '{}'.", namePackage.getImplName());
            }
            var func = new DistanceFunctionDesc(id, file.getOriginalFilename(), namePackage.getPackageName(), namePackage.getImplName(), file.getSize(), funcPath.toString());

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

    @SneakyThrows
    @Override
    public DistanceFunction getDistanceFunctionImpl(String distanceFunctionNameOrId) {
        if (isPredefined(distanceFunctionNameOrId)) {
            log.info("Getting predefined distance function: '{}'.", distanceFunctionNameOrId);
            switch (distanceFunctionNameOrId) {
                case DistanceFunction.PredefinedNames.EUCLIDEAN:
                    return new EuclideanDistance();
                case DistanceFunction.PredefinedNames.COSINE:
                    // TODO impl cosine distance function
                    throw new NotImplementedException("Cosine distance function not implemented yet");
                case DistanceFunction.PredefinedNames.NONE:
                default:
                    throw new IllegalStateException("No implementation for distance function");
            }
        }

        var id = findId(distanceFunctionNameOrId);
        var impl = distanceFunctionsImplMap.get(id);
        if (impl != null) {
            // TODO clear state when using cache if someone impl sth like this inside?
            return impl;
        }

        var file = load(id);
        var packageEvaluator = new ImplementationEvaluator();
        impl = packageEvaluator.callForDistanceFunction(file);
        distanceFunctionsImplMap.put(id, impl);
        return impl;
    }

    @Override
    public Map<String, DistanceFunctionDesc> allDistanceFunctionInfo() {
        return distanceFunctionsMap;
    }

    private boolean isPredefined(String name) {
        return DistanceFunction.PREDEFINED_FUNCTIONS.contains(name);
    }

    private String findId(@NonNull String nameOrId) {
        if (distanceFunctionsImplMap.containsKey(nameOrId)) {
            return nameOrId;
        } else {
            return distanceFunctionsMap.entrySet()
                    .stream()
                    .filter(entry -> nameOrId.equals(entry.getValue().getFunctionName()))
                    .findAny()
                    .map(Map.Entry::getKey)
                    .orElseThrow(() -> new IllegalArgumentException("Distance function not found with name: " + nameOrId));
        }
    }

    @PostConstruct
    void init() throws IOException {
        // TODO save on PreDestroy and collect or keep removed
        Files.createDirectories(Paths.get(distanceFunctionsPath));
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
