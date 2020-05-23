package pl.edu.pw.ddm.platform.agent.configuration.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConfigurationDto {

    private String localHostName;
    private String localHostIp;

}
