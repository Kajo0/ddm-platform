package pl.edu.pw.ddm.api.core.instance.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AlgorithmProcessingInfoDto {

    private String instanceId;
    private String processId;
    private String algorithm;
    private Integer nodes;
    private LocalDateTime start;
    private LocalDateTime finish;
    private Boolean success;
    private String error;

    public static AlgorithmProcessingInfoDto dummy() {
        return AlgorithmProcessingInfoDto.builder()
                .instanceId("null")
                .processId("null")
                .algorithm("aoptkm")
                .nodes(4)
                .start(LocalDateTime.now())
                .finish(null)
                .success(null)
                .error(null)
                .build();
    }

}
