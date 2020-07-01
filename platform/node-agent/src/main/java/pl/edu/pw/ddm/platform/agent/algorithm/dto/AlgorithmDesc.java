package pl.edu.pw.ddm.platform.agent.algorithm.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AlgorithmDesc {

    private String id;
    private String packageName;
    private String pipeline;

}
