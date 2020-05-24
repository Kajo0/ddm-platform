package pl.edu.pw.ddm.platform.core.execution.dto;

import java.util.Map;

import lombok.Data;

@Data
public class ExecutionStatsDto {

    private TimeStatsDto time;
    private TransferStatsDto transfer;

    @Data
    public static class TimeStatsDto {

        private Map<String, LocalStatsDto> localsProcessings;
        private Map<String, LocalStatsDto> localsUpdates;
        private Map<String, LocalStatsDto> localsExecutions;
        private long globalProcessing;
        private long ddmTotalProcessing;

        private long localProcessing;
        private long maxLocalProcessing;
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
        public static class LocalStatsDto {

            private Long processing;
            private Long loading;
        }
    }

    @Data
    public static class TransferStatsDto {

        private Map<String, Integer> localsBytes;
        private int globalBytes;
        private int localBytes;
    }

}
