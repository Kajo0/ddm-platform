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

            private long ddmTotalProcessing;
            private List<ProcessingWrapper> localsProcessings;
            private List<Long> globalProcessings;
            private Map<String, LocalStats> localsExecutions;

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
            static class ProcessingWrapper {

                private Map<String, LocalStats> map;
            }

            @Data
            static class LocalStats {

                private Long processing;
                private Long loading;
            }
        }

        @Data
        static class TransferStats {

            private List<Map<String, Integer>> localsBytes;
            private List<Integer> globalsBytes;
            private int localBytes;
            private int globalBytes;
        }
    }

}
