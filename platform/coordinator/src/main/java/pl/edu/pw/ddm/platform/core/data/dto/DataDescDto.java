package pl.edu.pw.ddm.platform.core.data.dto;

import java.util.List;

import lombok.Data;

@Data
public class DataDescDto {

    private String id;
    private String originalName;
    private String type;
    private Long sizeInBytes;
    private Long numberOfSamples;
    private String separator;
    private Integer idIndex;
    private Integer labelIndex;
    private Integer attributesAmount;
    private String[] colTypes;
    private List<String> filesLocations;
    private List<Long> sizesInBytes;
    private List<Long> numbersOfSamples;

}
