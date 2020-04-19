package pl.edu.pw.ddm.platform.core.results;

import java.io.File;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
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

@Service
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class LocalResultsValidationService implements ResultsValidationService {

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

        switch (algorithmType) {
            case MiningMethod.MethodType.CLUSTERING:
                return clusteringValidation(predictions, target, metrics);
            case MiningMethod.MethodType.CLASSIFICATION:
                return classificationValidation(predictions, target, metrics);

            default:
                throw new IllegalArgumentException("Unknown algorithm type: " + algorithmType);
        }
    }

    private MetricsSummary clusteringValidation(List<IdLabel> predictions, List<IdLabel> target, String[] metrics) {
        var results = new ClusteringMetrics(predictions, target);
        // TODO improve
        Stream.of(metrics)
                .forEach(metric -> {
                    if (Metrics.ADJUSTED_RAND_INDEX.equals(metric)) {
                        results.adjustedRandIndex();
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

}
