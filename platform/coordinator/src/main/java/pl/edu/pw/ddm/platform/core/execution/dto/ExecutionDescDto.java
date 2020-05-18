package pl.edu.pw.ddm.platform.core.execution.dto;

import java.time.LocalDateTime;

import lombok.Data;

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
    private LocalDateTime started;
    private LocalDateTime updated;
    private LocalDateTime stopped;

}
