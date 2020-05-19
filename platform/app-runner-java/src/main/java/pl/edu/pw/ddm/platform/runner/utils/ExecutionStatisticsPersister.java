package pl.edu.pw.ddm.platform.runner.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

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

    private final static String FILE = ControlFileNames.STATISTICS;

    @SneakyThrows
    public void save(String executionPath, Stats stats, String executionId) {
        Path path = Paths.get(executionPath, executionId, FILE);
        Files.createDirectories(path.getParent());
        Files.write(path, new ObjectMapper().writeValueAsString(stats).getBytes());
    }

    @Value(staticConstructor = "of")
    static class Stats {

        private TimeStats time;
        private TransferStats transfer;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class TimeStats {

        private long ddmTotalProcessing;

        @Singular
        private Map<String, LocalStats> localsProcessings;

        @Singular
        private Map<String, LocalStats> localsUpdates;

        @Singular
        private Map<String, LocalStats> localsExecutions;

        private long globalProcessing;

        @JsonProperty("localProcessing")
        long getLocalProcessing() {
            return Stream.of(localsProcessings.values(), localsUpdates.values())
                    .flatMap(Collection::stream)
                    .map(LocalStats::getProcessing)
                    .reduce(0L, Long::sum);
        }

        @JsonProperty("localLoading")
        long getLocalLoading() {
            return Stream.of(localsProcessings.values(), localsUpdates.values())
                    .flatMap(Collection::stream)
                    .map(LocalStats::getLoading)
                    .reduce(0L, Long::sum);
        }

        @JsonProperty("executionLoading")
        long getExecutionLoading() {
            return localsExecutions.values()
                    .stream()
                    .map(LocalStats::getLoading)
                    .reduce(0L, Long::sum);
        }

        @JsonProperty("total")
        long getTotal() {
            return getLocalProcessing() + globalProcessing;
        }

        @JsonProperty("totalExecution")
        long getTotalExecution() {
            return localsExecutions.values()
                    .stream()
                    .map(LocalStats::getProcessing)
                    .reduce(0L, Long::sum);
        }

        @JsonProperty("totalWithoutLoading")
        long getTotalWithoutLoading() {
            return getTotal() - getLocalLoading();
        }

        @JsonProperty("totalExecutionWithoutLoading")
        long getTotalExecutionWithoutLoading() {
            return getTotalExecution() - getExecutionLoading();
        }


        @JsonProperty("ddmTotalWithoutLoading")
        long getDdmTotalWithoutLoading() {
            return ddmTotalProcessing - getLocalLoading();
        }

        @Value(staticConstructor = "of")
        static class LocalStats {

            private Long processing;
            private Long loading;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class TransferStats {

        // TODO add preprocessing etc.

        @Singular
        private Map<String, Integer> localsBytes;

        private int globalBytes;

        @JsonProperty("localBytes")
        int getLocalBytes() {
            return localsBytes.values()
                    .stream()
                    .reduce(0, Integer::sum);
        }
    }

}
