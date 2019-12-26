package pl.edu.pw.ddm.api.core.instance.dto;

import java.util.Map;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Value
@Builder
public class InstanceConfigDto {

    private String instanceId;
    private String ddmModel;
    private Integer nodes;
    private Map<String, DataDistributionDto> dataDistribution;
    private Boolean ready;
    private Boolean busy;
    private Boolean healthy;

    public static InstanceConfigDto dummy() {
        return new InstanceConfigDto("dummy", "central", 4, Map.of(), true, false, true);
    }

    @Data
    public static class DataDistributionDto {}

}
