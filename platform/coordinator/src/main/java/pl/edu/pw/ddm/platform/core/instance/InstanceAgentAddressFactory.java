package pl.edu.pw.ddm.platform.core.instance;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

@UtilityClass
public class InstanceAgentAddressFactory {

    public String sendData(InstanceAddrDto dto) {
        return base(dto) + "data/load";
    }

    public String sendAlgorithm(InstanceAddrDto dto) {
        return base(dto) + "algorithm/load";
    }

    public String startExecution(InstanceAddrDto dto, @NonNull String instanceId, @NonNull String algorithmId, @NonNull String dataId) {
        return base(dto) + "execution/run/" + instanceId + "/" + algorithmId + "/" + dataId;
    }

    private String base(InstanceAddrDto dto) {
        return "http://" + dto.agentAddress() + "/agent/";
    }

}
