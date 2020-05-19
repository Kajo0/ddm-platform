package pl.edu.pw.ddm.platform.runner.utils;

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

@AllArgsConstructor(staticName = "of")
public class ExecutionStatusPersister {
    // FIXME change also in node-agent LocalExecutionStatusPersister as this class is copied there to update status when stopping app

    private final static String FILE = ControlFileNames.STATUS;

    private final String executionPath;
    private final String executionId;

    public void init(String appId) {
        ExecutionStatus status = ExecutionStatus.builder()
                .appId(appId)
                .startTime(LocalDateTime.now())
                .lastUpdate(LocalDateTime.now())
                .stage(ExecutionStage.INITIALIZING.code)
                .build();
        save(status);
    }

    // TODO handle errors somehow by catching it and rethrowing
    public void error(String msg) {
        ExecutionStatus status = load(executionId);
        status.setStage(ExecutionStage.ERROR.code);
        status.setMessage(msg);
        save(status);
    }

    public void started() {
        next(ExecutionStage.STARTED);
    }

    public void processLocal() {
        next(ExecutionStage.PROCESSING_LOCAL);
    }

    public void processGlobal() {
        next(ExecutionStage.PROCESSING_GLOBAL);
    }

    public void updateLocal() {
        next(ExecutionStage.UPDATING_LOCAL);
    }

    public void validate() {
        next(ExecutionStage.VALIDATION);
    }

    public void summarize() {
        next(ExecutionStage.SUMMARIZING);
    }

    public void stop() {
        next(ExecutionStage.STOPPED);
    }

    public void finish() {
        next(ExecutionStage.FINISHED);
    }

    @SneakyThrows
    public ExecutionStatus load(String executionId) {
        Path path = Paths.get(executionPath, executionId, FILE);
        return new ObjectMapper().readValue(path.toFile(), ExecutionStatus.class);
    }

    private void next(ExecutionStage stage) {
        ExecutionStatus status = load(executionId);
        status.next(stage);
        save(status);
    }

    @SneakyThrows
    private void save(ExecutionStatus status) {
        Path path = Paths.get(executionPath, executionId, FILE);
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
