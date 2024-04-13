package pl.edu.pw.ddm.platform.core.execution.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExecutionDescDto {

    private String id;
    private String instanceId;
    private String algorithmId;
    private String distanceFunctionId;
    private String distanceFunctionName;
    private String trainDataId;
    private String testDataId;
    private String status;
    private String message;
    private String appId;
    private String executionParams;
    private LocalDateTime started;
    private LocalDateTime updated;
    private LocalDateTime stopped;

}
