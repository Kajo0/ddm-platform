package pl.edu.pw.ddm.platform.core.execution.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ExecutionStatsDto {

    private TimeStatsDto time;
    private TransferStatsDto transfer;
    private DataStatsDto data;
    private CustomMetrics custom;

    @Data
    public static class TimeStatsDto {

        private long ddmTotalProcessing;
        private long ddmTotalTrainingProcessing;
        private long ddmTotalValidationProcessing;

        private List<ProcessingWrapper> localsProcessings;
        private List<Long> globalProcessings;
        private Map<String, LocalStatsDto> localsExecutions;

        private long localProcessing;
        private long globalProcessing;
        private long maxLocalProcessing;
        private long maxGlobalProcessing;
        private long localLoading;
        private long maxLocalLoading;
        private long executionLoading;
        private long maxExecutionLoading;
        private long total;
        private long totalMaxProcessing;
        private long totalExecution;
        private long maxExecution;
        private long totalWithoutLoading;
        private long totalExecutionWithoutLoading;
        private long ddmTotalWithoutMaxLoadings;

        @Data
        public static class ProcessingWrapper {

            private Map<String, LocalStatsDto> map;
        }

        @Data
        public static class LocalStatsDto {

            private Long processing;
            private Long loading;
        }
    }

    @Data
    public static class TransferStatsDto {

        private List<Map<String, Integer>> localsBytes;
        private List<Integer> globalsBytes;
        private int globalMethodBytes;
        private int localBytes;
        private int globalBytes;
    }

    @Data
    public static class DataStatsDto {

        private List<Map<String, Integer>> trainingsBytes;
        private int trainingBytes;
    }

    @Data
    public static class CustomMetrics {

        private List<Map<String, String>> locals;
        private List<String> globals;
    }

}
