package pl.edu.pw.ddm.platform.agent.runner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
class LocalExecutionStatusPersister {
    // TODO make common save model with app-runner-java as this class is copy

    @Value("${paths.execution.path}")
    private String executionPath;

    @Value("${paths.execution.status-filename}")
    private String statusFilename;

    public void init(String executionId, String appId) {
        ExecutionStatus status = ExecutionStatus.builder()
                .appId(appId)
                .startTime(LocalDateTime.now())
                .lastUpdate(LocalDateTime.now())
                .stage(ExecutionStage.INITIALIZING.code)
                .build();
        save(executionId, status);
    }

    public void stop(String executionId) throws IOException {
        ExecutionStatus status = load(executionId);
        status.next(ExecutionStage.STOPPED);
        status.setMessage("Stop on demand.");
        save(executionId, status);
    }

    public ExecutionStatus load(String executionId) throws IOException {
        Path path = Paths.get(executionPath, executionId, statusFilename);
        return new ObjectMapper().readValue(path.toFile(), ExecutionStatus.class);
    }

    @SneakyThrows
    private void save(String executionId, ExecutionStatus status) {
        Path path = Paths.get(executionPath, executionId, statusFilename);
        Files.createDirectories(path.getParent());
        Files.write(path, new ObjectMapper().writeValueAsString(status).getBytes());
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionStatus {

        private String appId;
        private String stage;
        private String message;

        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        private LocalDateTime startTime;

        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        private LocalDateTime lastUpdate;

        @Singular("hist")
        private List<ExecutionEntry> history;

        void next(ExecutionStage nextStage) {
            history.add(ExecutionEntry.of(stage, lastUpdate, LocalDateTime.now()));
            stage = nextStage.code;
            lastUpdate = LocalDateTime.now();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor(staticName = "of")
    public static class ExecutionEntry {

        private String stage;

        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        private LocalDateTime start;

        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        private LocalDateTime stop;
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum ExecutionStage {
        INITIALIZING("INITIALIZING"),
        ERROR("ERROR"),
        STARTED("STARTED"),
        STOPPED("STOPPED"),
        PROCESSING_LOCAL("PROCESSING_LOCAL"),
        PROCESSING_GLOBAL("PROCESSING_GLOBAL"),
        UPDATING_LOCAL("UPDATING_LOCAL"),
        VALIDATION("VALIDATION"),
        SUMMARIZING("SUMMARIZING"),
        FINISHED("FINISHED");

        private final String code;
    }

}
