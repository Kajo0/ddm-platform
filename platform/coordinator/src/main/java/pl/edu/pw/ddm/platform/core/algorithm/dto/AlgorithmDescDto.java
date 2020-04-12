package pl.edu.pw.ddm.platform.core.algorithm.dto;

import lombok.Data;

@Data
public class AlgorithmDescDto {

    private String id;
    private String name;
    private String packageName;
    private String type;
    private Long sizeInBytes;
    private String location;

}
