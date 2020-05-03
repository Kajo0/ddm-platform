package pl.edu.pw.ddm.platform.core.execution;

import java.io.File;
import java.util.List;
import java.util.Map;

import lombok.Data;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

interface ResultsCollector {

    String collect(List<InstanceAddrDto> addresses, ExecutionStarter.ExecutionDesc desc);

    File[] load(String executionId);

    ExecutionStats loadStats(String executionId);

    @Data
    class ExecutionStats {

        private TimeStats time;
        private TransferStats transfer;

        @Data
        static class TimeStats {

            private Map<String, LocalStats> localsProcessings;
            private Map<String, LocalStats> localsUpdates;
            private Map<String, LocalStats> localsExecutions;
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
            static class LocalStats {

                private Long processing;
                private Long loading;
            }
        }

        @Data
        static class TransferStats {

            private Map<String, Integer> localsBytes;
            private int globalBytes;
            private int localBytes;
        }
    }

}
