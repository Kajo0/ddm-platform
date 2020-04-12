package pl.edu.pw.ddm.platform.agent.data.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DistanceFunctionDesc {

    private String id;
    private String packageName;
    private String functionName;

}
