package pl.edu.pw.ddm.platform.core.execution.dto;

import lombok.Data;

@Data
public class ExecutionDescDto {

    private String id;
    private String instanceId;
    private String algorithmId;
    private String dataId;
    private String status;
    private String message;

}
