package pl.edu.pw.ddm.platform.core.instance.dto;

import lombok.Data;

@Data
public class InstanceAddrDto {

    private String id;
    private String name;
    private String type;
    private String address;
    private String port;
    private String uiPort;
    private String agentPort;

    public String agentAddress() {
        return address + ":" + agentPort;
    }

}
