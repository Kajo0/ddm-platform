package pl.edu.pw.ddm.platform.core.data.dto;

import lombok.Data;

@Data
public class DistanceFunctionDescDto {

    private String id;
    private String originalName;
    private String packageName;
    private String functionName;
    private Long sizeInBytes;
    private String location;

}
