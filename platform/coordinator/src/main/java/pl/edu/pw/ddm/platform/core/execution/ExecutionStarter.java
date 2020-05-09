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

    String start(InstanceAddrDto masterAddr, String instanceId, String algorithmId, String trainDataId, String testDataId, String distanceFunctionId, String distanceFunctionName, Map<String, String> executionParams);

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
        private final String trainDataId;
        private final String testDataId;
        private final String distanceFunctionId;
        private final String distanceFunctionName;
        private final InstanceAddrDto masterAddr;
        private final ExecutionStatus status;

        @Builder.Default
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        private LocalDateTime started = LocalDateTime.now();

        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
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
