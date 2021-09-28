package pl.edu.pw.ddm.platform.core.data;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pw.ddm.platform.core.util.IdGenerator;
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy;
import pl.edu.pw.ddm.platform.strategies.PartitionerStrategies;
import pl.edu.pw.ddm.platform.strategies.SeparateByLabelsPartitionerStrategy;
import pl.edu.pw.ddm.platform.strategies.UnbalancedPartitionerStrategy;
import pl.edu.pw.ddm.platform.strategies.UniformPartitionerStrategy;
import pl.edu.pw.ddm.platform.strategies.conceptdrift.ConceptDriftPartitionerStrategy;
import pl.edu.pw.ddm.platform.strategies.covariateshift.CovariateShiftPartitionerStrategy;
import pl.edu.pw.ddm.platform.strategies.mostof.MostOfOnePlusSomePartitionerStrategy;

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

@Slf4j
@Service
class LocalPartitionerStrategyLoader implements PartitionerStrategyLoader {

    @Value("${paths.partitioner-strategies}")
    private String partitionerStrategiesPath;

    private final Map<String, StrategyDesc> partitionerStrategiesMap = new HashMap<>();
    private final Map<String, PartitionerStrategy> partitionerStrategiesImplMap = new HashMap<>();

    @SneakyThrows
    @Override
    public String save(MultipartFile file) {
        var id = IdGenerator.generate(file.getOriginalFilename() + file.getSize());

        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        Path funcPath = Paths.get(partitionerStrategiesPath, id + "." + ext);
        Files.write(funcPath, file.getBytes());

        try {
            var namePackage = new ImplementationEvaluator()
                    .callForPartitionerStrategyName(funcPath.toFile());
            if (isPredefined(namePackage.getImplName())) {
                log.warn("Tried to add strategy with one of predefined names: '{}'.", namePackage.getImplName());
                throw new IllegalArgumentException("Predefined strategy name is forbidden.");
            }
            var func = new StrategyDesc(id,
                    file.getOriginalFilename(),
                    namePackage.getPackageName(),
                    namePackage.getImplName(),
                    file.getSize(),
                    funcPath.toString());

            if (partitionerStrategiesMap.put(id, func) != null) {
                log.warn("Loaded probably the same strategy as before with id '{}'.", id);
            }
            return id;
        } catch (Exception e) {
            log.error("Error during strategy name evaluation so removing saved jar.", e);
            Files.delete(funcPath);
            throw e;
        }
    }

    @Override
    public File load(String strategyId) {
        var desc = getStrategyDesc(strategyId);
        log.debug("Loading strategy with id '{}' from '{}'.", strategyId, desc.getLocation());

        return Optional.of(desc)
                .map(StrategyDesc::getLocation)
                .map(Path::of)
                .map(Path::toFile)
                .get();
    }

    @Override
    public StrategyDesc getStrategyDesc(String strategyId) {
        return partitionerStrategiesMap.get(strategyId);
    }

    @SneakyThrows
    @Override
    public PartitionerStrategy getStrategyImpl(String strategyNameOrId) {
        if (isPredefined(strategyNameOrId)) {
            log.info("Getting predefined strategy: '{}'.", strategyNameOrId);
            // TODO make it more generic by creating instance instead of manual creation
            switch (strategyNameOrId) {
                case PartitionerStrategies.UNIFORM:
                    return new UniformPartitionerStrategy();
                case PartitionerStrategies.SEPARATE_LABELS:
                    return new SeparateByLabelsPartitionerStrategy();
                case PartitionerStrategies.MOST_OF_ONE_PLUS_SOME:
                    return new MostOfOnePlusSomePartitionerStrategy();
                case PartitionerStrategies.UNBALANCEDNESS:
                    return new UnbalancedPartitionerStrategy();
                case PartitionerStrategies.COVARIATE_SHIFT:
                    return new CovariateShiftPartitionerStrategy();
                case PartitionerStrategies.CONCEPT_DRIFT:
                    return new ConceptDriftPartitionerStrategy();
                default:
                    throw new IllegalStateException("No implementation for predefined strategy: " + strategyNameOrId);
            }
        }

        var id = findId(strategyNameOrId);
        var impl = partitionerStrategiesImplMap.get(id);
        if (impl != null) {
            // TODO clear state when using cache if someone impl sth like this inside?
            return impl;
        }

        var file = load(id);
        var packageEvaluator = new ImplementationEvaluator();
        impl = packageEvaluator.callForPartitionerStrategy(file);
        partitionerStrategiesImplMap.put(id, impl);
        return impl;
    }

    @Override
    public Map<String, StrategyDesc> allStrategiesInfo() {
        return partitionerStrategiesMap;
    }

    private boolean isPredefined(String name) {
        return PartitionerStrategies.PREDEFINED_STRATEGIES.contains(name);
    }

    private String findId(@NonNull String nameOrId) {
        if (partitionerStrategiesImplMap.containsKey(nameOrId)) {
            return nameOrId;
        } else {
            return partitionerStrategiesMap.entrySet()
                    .stream()
                    .filter(entry -> nameOrId.equals(entry.getValue().getStrategyName()))
                    .findAny()
                    .map(Map.Entry::getKey)
                    .orElseThrow(() -> new IllegalArgumentException("Partitioning strategy not found with name: " + nameOrId));
        }
    }

    @PostConstruct
    void init() throws IOException {
        // TODO save on PreDestroy and collect or keep removed
        Files.createDirectories(Paths.get(partitionerStrategiesPath));
    }

    @PreDestroy
    void destroy() {
        // TODO disable for persistent config
        log.info("PreDestroy " + this.getClass().getSimpleName());
        partitionerStrategiesMap.values()
                .parallelStream()
                .map(StrategyDesc::getLocation)
                .forEach(location -> {
                    try {
                        log.debug("Deleting partitioning strategy: '{}'.", location);
                        Files.deleteIfExists(Path.of(location));
                    } catch (IOException e) {
                        log.error("Failed to remove partitioning strategy file: '{}'.", location);
                    }
                });
    }

}
