package pl.edu.pw.ddm.platform.core.execution;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

interface ExecutionStarter {

    String start(InstanceAddrDto masterAddr, String instanceId, String algorithmId, String dataId, String distanceFunctionId, String distanceFunctionName);

    String stop(String executionId);

    ExecutionDesc status(String executionId);

    Map<String, ExecutionDesc> allExecutionsInfo();

    @Data
    @Builder
    @AllArgsConstructor
    @RequiredArgsConstructor
    class ExecutionDesc {

        private final String id;
        private final String instanceId;
        private final String algorithmId;
        private final String dataId;
        private final String distanceFunctionId;
        private final String distanceFunctionName;
        private final InstanceAddrDto masterAddr;
        private final ExecutionStatus status;

        @Builder.Default
        private LocalDateTime started = LocalDateTime.now();
        private LocalDateTime stopped;
        private String message;

        @Getter
        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        enum ExecutionStatus {
            STARTED("started"),
            STOPPED("stopped"),
            FAILED("failed"),
            FINISHED("finished");

            private final String code;
        }
    }

}
