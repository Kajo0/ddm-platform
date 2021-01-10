package pl.edu.pw.ddm.platform.runner.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ExecutionStatisticsPersister {

    private final String FILE = ControlFileNames.STATISTICS;

    @SneakyThrows
    public void save(String executionPath, Stats stats, String executionId) {
        Path path = Paths.get(executionPath, executionId, FILE);
        Files.createDirectories(path.getParent());
        Files.write(path, new ObjectMapper().writeValueAsString(stats).getBytes());
    }

    @Value(staticConstructor = "of")
    static class Stats {

        TimeStats time;
        TransferStats transfer;
        DataStats data;
        CustomMetrics custom;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class TimeStats {

        private long ddmTotalProcessing;
        private long ddmTotalTrainingProcessing;
        private long ddmTotalValidationProcessing;

        @Singular
        private List<ProcessingWrapper> localsProcessings;

        @Singular
        private List<Long> globalProcessings;

        @Singular
        private Map<String, LocalStats> localsExecutions;

        @JsonProperty("localProcessing")
        long getLocalProcessing() {
            return localsProcessings.stream()
                    .map(ProcessingWrapper::getMap)
                    .map(Map::values)
                    .flatMap(Collection::stream)
                    .map(LocalStats::getProcessing)
                    .reduce(0L, Long::sum);
        }

        @JsonProperty("globalProcessing")
        long getGlobalProcessing() {
            return globalProcessings.stream()
                    .reduce(0L, Long::sum);
        }

        @JsonProperty("maxLocalProcessing")
        long getMaxLocalProcessing() {
            return localsProcessings.stream()
                    .map(ProcessingWrapper::getMap)
                    .map(Map::values)
                    .flatMap(Collection::stream)
                    .map(LocalStats::getProcessing)
                    .max(Long::compare)
                    .orElse(0L);
        }

        @JsonProperty("maxGlobalProcessing")
        long getMaxGlobalProcessing() {
            return globalProcessings.stream()
                    .max(Long::compare)
                    .orElse(0L);
        }

        @JsonProperty("localLoading")
        long getLocalLoading() {
            return localsProcessings.stream()
                    .map(ProcessingWrapper::getMap)
                    .map(Map::values)
                    .flatMap(Collection::stream)
                    .map(LocalStats::getLoading)
                    .reduce(0L, Long::sum);
        }

        @JsonProperty("maxLocalLoading")
        long getMaxLocalLoading() {
            return localsProcessings.stream()
                    .map(ProcessingWrapper::getMap)
                    .map(Map::values)
                    .flatMap(Collection::stream)
                    .map(LocalStats::getLoading)
                    .max(Long::compare)
                    .orElse(0L);
        }

        @JsonProperty("executionLoading")
        long getExecutionLoading() {
            return localsExecutions.values()
                    .stream()
                    .map(LocalStats::getLoading)
                    .reduce(0L, Long::sum);
        }

        @JsonProperty("maxExecutionLoading")
        long getMaxExecutionLoading() {
            return localsExecutions.values()
                    .stream()
                    .map(LocalStats::getLoading)
                    .max(Long::compare)
                    .orElse(0L);
        }

        @JsonProperty("total")
        long getTotal() {
            return getLocalProcessing() + getGlobalProcessing();
        }

        @JsonProperty("totalMaxProcessing")
        long getTotalMaxProcessing() {
            long maxLocalProcessings = localsProcessings.stream()
                    .map(ProcessingWrapper::getMap)
                    .map(Map::values)
                    .map(values -> values.stream()
                            .map(LocalStats::getProcessing)
                            .max(Long::compare)
                            .orElse(0L)
                    )
                    .reduce(0L, Long::sum);
            return maxLocalProcessings + getMaxGlobalProcessing();
        }

        @JsonProperty("totalExecution")
        long getTotalExecution() {
            return localsExecutions.values()
                    .stream()
                    .map(LocalStats::getProcessing)
                    .reduce(0L, Long::sum);
        }

        @JsonProperty("maxExecution")
        long getMaxExecution() {
            return localsExecutions.values()
                    .stream()
                    .map(LocalStats::getProcessing)
                    .max(Long::compare)
                    .orElse(0L);
        }

        @JsonProperty("totalWithoutLoading")
        long getTotalWithoutLoading() {
            return getTotal() - getLocalLoading();
        }

        @JsonProperty("totalExecutionWithoutLoading")
        long getTotalExecutionWithoutLoading() {
            return getTotalExecution() - getExecutionLoading();
        }

        @JsonProperty("ddmTotalWithoutMaxLoadings")
        long getDdmTotalWithoutMaxLoadings() {
            return ddmTotalProcessing - getMaxLocalLoading() - getMaxExecutionLoading();
        }

        @Value(staticConstructor = "of")
        static class ProcessingWrapper {

            Map<String, LocalStats> map;
        }

        @Value(staticConstructor = "of")
        static class LocalStats {

            Long processing;
            Long loading;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class TransferStats {

        // TODO add preprocessing etc.

        @Singular
        private List<Map<String, Integer>> localsBytes;

        @Singular
        private List<Integer> globalsBytes;

        private int globalMethodBytes;

        @JsonProperty("localBytes")
        int getLocalBytes() {
            return localsBytes.stream()
                    .map(Map::values)
                    .flatMap(Collection::stream)
                    .reduce(0, Integer::sum);
        }

        @JsonProperty("globalBytes")
        int getGlobalBytes() {
            return globalsBytes.stream()
                    .reduce(0, Integer::sum) - globalMethodBytes;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class DataStats {

        @Singular
        private List<Map<String, Integer>> trainingsBytes;

        @JsonProperty("trainingBytes")
        int getTrainingBytes() {
            return trainingsBytes.stream()
                    .map(Map::values)
                    .flatMap(Collection::stream)
                    .filter(Objects::nonNull)
                    .reduce(0, Integer::sum);
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class CustomMetrics {

        @Singular
        private List<Map<String, String>> locals;

        @Singular
        private List<String> globals;
    }

}
