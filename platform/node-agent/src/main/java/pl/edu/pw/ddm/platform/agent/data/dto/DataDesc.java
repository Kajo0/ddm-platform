package pl.edu.pw.ddm.platform.agent.data.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataDesc {

    private String id;
    private String separator;
    private Integer idIndex;
    private Integer labelIndex;
    private Integer attributesAmount;
    private String[] colTypes;

}
