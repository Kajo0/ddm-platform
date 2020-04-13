package pl.edu.pw.ddm.platform.core.algorithm.dto;

import lombok.Data;

@Data
public class AlgorithmDescDto {

    private String id;
    private String originalName;
    private String packageName;
    private String algorithmType;
    private String algorithmName;
    private Long sizeInBytes;
    private String location;

}
