package pl.edu.pw.ddm.platform.core.execution;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

interface ExecutionStarter {

    String start(InstanceAddrDto masterAddr, String instanceId, String algorithmId, String trainDataId, String testDataId, String distanceFunctionId, String distanceFunctionName, Integer cpuCores, Integer memoryInGb, Map<String, String> executionParams);

    String stop(String executionId);

    ExecutionDesc status(String executionId);

    Map<String, ExecutionDesc> allExecutionsInfo();

    @Data
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @RequiredArgsConstructor
    class ExecutionDesc {

        private final String id;
        private final String instanceId;
        private final String algorithmId;
        private final String trainDataId;
        private final String testDataId;
        private final String distanceFunctionId;
        private final String distanceFunctionName;
        private final InstanceAddrDto masterAddr;
        private final ExecutionStatus status;
        private final String appId;
        private final String executionParams;

        @Builder.Default
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        private LocalDateTime started = LocalDateTime.now();

        @Builder.Default
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        private LocalDateTime updated = LocalDateTime.now();

        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        private LocalDateTime stopped;

        private String message;

        boolean isRunning() {
            return status == ExecutionStatus.INITIALIZING ||
                    status == ExecutionStatus.STARTED;
        }

        boolean isCompleted() {
            return status == ExecutionStatus.FINISHED;
        }

        @Getter
        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        enum ExecutionStatus {
            INITIALIZING("initializing"),
            STARTED("started"),
            STOPPED("stopped"),
            FAILED("failed"),
            FINISHED("finished");

            private final String code;
        }
    }

    @Data
    class ExecutionAgentStatus {

        private String appId;
        private String stage;
        private String message;
        private LocalDateTime startTime;
        private LocalDateTime lastUpdate;
    }

}
