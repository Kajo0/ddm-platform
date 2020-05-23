package pl.edu.pw.ddm.platform.core.instance.dto;

import lombok.Data;

@Data
public class InstanceAddrDto {

    private String id;
    private String name;
    private String type;
    private String address;
    private String localhostName;
    private String localhostIp;
    private boolean alive;
    private String port;
    private String uiPort;
    private String agentPort;
    private Integer cpu;
    private Integer memory;
    private Integer disk;

    public String agentAddress() {
        return address + ":" + agentPort;
    }

    public boolean isMaster() {
        return "master".equals(type);
    }

    public boolean isWorker() {
        return "worker".equals(type);
    }

}
