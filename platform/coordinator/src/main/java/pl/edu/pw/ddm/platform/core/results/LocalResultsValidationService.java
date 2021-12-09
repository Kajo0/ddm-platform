package pl.edu.pw.ddm.platform.core.results;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.edu.pw.ddm.platform.core.data.DataFacade;
import pl.edu.pw.ddm.platform.core.data.dto.DataDescDto;
import pl.edu.pw.ddm.platform.core.execution.ExecutionResultsFacade;
import pl.edu.pw.ddm.platform.interfaces.mining.MiningMethod;
import pl.edu.pw.ddm.platform.metrics.ClassificationMetrics;
import pl.edu.pw.ddm.platform.metrics.ClusteringMetrics;
import pl.edu.pw.ddm.platform.metrics.Metrics;
import pl.edu.pw.ddm.platform.metrics.dto.IdLabel;
import pl.edu.pw.ddm.platform.metrics.dto.MetricsSummary;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class LocalResultsValidationService implements ResultsValidationService {

    @Value("${paths.results.path}")
    private String resultsPath;

    @Value("${paths.results.validation-filename}")
    private String validationFilename;

    private final DataFacade dataFacade;
    private final ExecutionResultsFacade executionResultsFacade;

    @SneakyThrows
    @Override
    public MetricsSummary validate(@NonNull String executionId, String dataId, String algorithmType, String[] metrics) {
        var dataFileReq = DataFacade.LoadDataFileRequest.of(dataId);
        File dataFile = dataFacade.dataFile(dataFileReq);

        var dataDescReq = DataFacade.DescriptionRequest.of(dataId);
        DataDescDto dataDesc = dataFacade.description(dataDescReq);

        var resultsReq = ExecutionResultsFacade.LoadResultFilesRequest.of(executionId);
        File[] resultsFiles = executionResultsFacade.nodesResultsFiles(resultsReq);

        var target = loadPairs(dataFile, dataDesc.getSeparator(), dataDesc.getIdIndex(), dataDesc.getLabelIndex());
        var predictions = Stream.of(resultsFiles)
                .map(f -> loadPairs(f, ",", 0, 1))
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableList());

        MetricsSummary summary;
        switch (algorithmType) {
            case MiningMethod.MethodType.CLUSTERING:
                summary = clusteringValidation(predictions, target, metrics);
                break;
            case MiningMethod.MethodType.CLASSIFICATION:
                summary = classificationValidation(predictions, target, metrics);
                break;

            default:
                throw new IllegalArgumentException("Unknown algorithm type: " + algorithmType);
        }

        saveMetrics(summary, executionId);

        return summary;
    }

    private MetricsSummary clusteringValidation(List<IdLabel> predictions, List<IdLabel> target, String[] metrics) {
        var results = new ClusteringMetrics(predictions, target);
        // TODO improve
        Stream.of(metrics)
                .forEach(metric -> {
                    switch (metric) {
                        case Metrics.ADJUSTED_RAND_INDEX:
                            results.adjustedRandIndex();
                            break;
                        case Metrics.ADJUSTED_MUTUAL_INFORMATION:
                            results.adjustedMutualInformation();
                            break;
                    }
                });

        return results.getSummary();
    }

    private MetricsSummary classificationValidation(List<IdLabel> predictions, List<IdLabel> target, String[] metrics) {
        var results = new ClassificationMetrics(predictions, target);
        // TODO improve
        Stream.of(metrics)
                .forEach(metric -> {
                    if (Metrics.ACCURACY.equals(metric)) {
                        results.accuracy();
                    } else if (Metrics.PRECISION.equals(metric)) {
                        results.precision();
                    } else if (Metrics.RECALL.equals(metric)) {
                        results.recall();
                    } else if (Metrics.F_MEASURE.equals(metric)) {
                        results.fMeasure();
                    }
                });

        return results.getSummary();
    }

    @SneakyThrows
    private List<IdLabel> loadPairs(File file, String separator, int idIndex, int labelIndex) {
        return Files.readAllLines(file.toPath())
                .stream()
                .map(line -> line.split(separator))
                .map(idLabel -> IdLabel.of(idLabel[idIndex].trim(), idLabel[labelIndex].trim()))
                .collect(Collectors.toUnmodifiableList());
    }

    private void saveMetrics(MetricsSummary summary, String executionId) throws IOException {
        ValidationMetrics metrics = loadMetrics(executionId);
        metrics.getMetrics()
                .putAll(summary.getMetrics());

        Path path = Paths.get(resultsPath, executionId, validationFilename);
        log.info("Saving file '{}' with execution validation metrics.", path);
        String json = new ObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValueAsString(metrics);
        Files.write(path, json.getBytes());
    }

    @SneakyThrows
    private ValidationMetrics loadMetrics(String executionId) {
        Path path = Paths.get(resultsPath, executionId, validationFilename);
        if (path.toFile().exists()) {
            log.info("Loading validation metrics file for execution id: '{}'.", executionId);
            return new ObjectMapper().readValue(path.toFile(), ValidationMetrics.class);
        } else {
            log.info("Validation metrics file for execution id: '{}' not exists.", executionId);
            return new ValidationMetrics();
        }
    }

}
