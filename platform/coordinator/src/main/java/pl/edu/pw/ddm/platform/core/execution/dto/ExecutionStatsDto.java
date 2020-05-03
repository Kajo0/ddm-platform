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
        private long localLoading;
        private long executionLoading;
        private long total;
        private long totalExecution;
        private long totalWithoutLoading;
        private long totalExecutionWithoutLoading;
        private long ddmTotalWithoutLoading;

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
