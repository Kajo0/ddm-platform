package pl.edu.pw.ddm.platform.core.instance;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

@UtilityClass
public class InstanceAgentAddressFactory {

    public String sendData(@NonNull InstanceAddrDto dto) {
        return base(dto) + "data/load";
    }

    public String sendDistanceFunction(@NonNull InstanceAddrDto dto) {
        return base(dto) + "data/distance-function/load";
    }

    public String sendAlgorithm(@NonNull InstanceAddrDto dto) {
        return base(dto) + "algorithm/load";
    }

    public String startExecution(@NonNull InstanceAddrDto dto,
                                 @NonNull String instanceId,
                                 @NonNull String algorithmId,
                                 @NonNull String trainDataId) {
        return base(dto) + "execution/run/" + instanceId + "/" + algorithmId + "/" + trainDataId;
    }

    public String collectResults(@NonNull InstanceAddrDto dto, @NonNull String executionId) {
        return base(dto) + "results/" + executionId + "/download";
    }

    private String base(@NonNull InstanceAddrDto dto) {
        return "http://" + dto.agentAddress() + "/agent/";
    }

}
