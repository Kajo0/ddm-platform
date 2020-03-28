package pl.edu.pw.ddm.platform.core.instance;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

@UtilityClass
public class InstanceAgentAddressFactory {

    public String sendData(InstanceAddrDto dto, @NonNull String dataId) {
        return base(dto) + "data/load/" + dataId;
    }

    private String base(InstanceAddrDto dto) {
        return "http://" + dto.agentAddress() + "/agent/";
    }

}
