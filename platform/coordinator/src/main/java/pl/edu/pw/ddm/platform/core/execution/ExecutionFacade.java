package pl.edu.pw.ddm.platform.core.execution;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.pw.ddm.platform.core.data.DistanceFunctionFacade;
import pl.edu.pw.ddm.platform.core.data.dto.DistanceFunctionDescDto;
import pl.edu.pw.ddm.platform.core.execution.dto.ExecutionDescDto;
import pl.edu.pw.ddm.platform.core.instance.InstanceFacade;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;

@Slf4j
@Service
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ExecutionFacade {

    private final InstanceFacade instanceFacade;
    private final DistanceFunctionFacade distanceFunctionFacade;
    private final ExecutionStarter executionStarter;

    public String start(@NonNull StartRequest request) {
        InstanceAddrDto masterAddr = findMasterAddress(request.instanceId);

        String distanceFunctionId = request.executionParams.get("distanceFunctionId");
        String distanceFunctionName = request.executionParams.get("distanceFunctionName");
        if (distanceFunctionId != null) {
            var req = DistanceFunctionFacade.DescriptionRequest.of(distanceFunctionId);
            DistanceFunctionDescDto desc = distanceFunctionFacade.description(req);
            distanceFunctionName = desc.getFunctionName();
        } else if (DistanceFunction.PREDEFINED_FUNCTIONS.contains(request.getDistanceFunctionName())) {
            distanceFunctionId = null;
            distanceFunctionName = request.getDistanceFunctionName();
        } else {
            throw new IllegalArgumentException("Unknown distance (id=" + distanceFunctionId + ", name=" + distanceFunctionName + ")");
        }
        // TODO add precondition to avoid both distance function id and name which does not matche id

        // TODO handle when cpu/memory not equal for all
        return executionStarter.start(masterAddr, request.instanceId, request.algorithmId, request.trainDataId, request.testDataId, distanceFunctionId, distanceFunctionName, masterAddr.getCpu(), masterAddr.getMemory(), request.executionParams);
    }

    public String stop(@NonNull StopRequest request) {
        return executionStarter.stop(request.getExecutionId());
    }

    public ExecutionDescDto status(@NonNull StatusRequest request) {
        return Optional.of(request)
                .map(StatusRequest::getExecutionId)
                .map(executionStarter::status)
                .map(ExecutionDtosMapper.INSTANCE::map)
                .get();
    }

    // TODO remove debug
    public String info() {
        try {
            return new ObjectMapper().writeValueAsString(executionStarter.allExecutionsInfo());
        } catch (JsonProcessingException e) {
            return executionStarter.allExecutionsInfo()
                    .toString();
        }
    }

    private InstanceAddrDto findMasterAddress(@NonNull String instanceId) {
        var req = InstanceFacade.AddressRequest.of(instanceId);
        return instanceFacade.addresses(req)
                .stream()
                .filter(InstanceAddrDto::isMaster)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No master node for instance: " + instanceId));
    }

    @Builder
    public static class StartRequest {

        @NonNull
        private final String instanceId;

        @NonNull
        private final String algorithmId;

        @NonNull
        private final String trainDataId;

        private final String testDataId;
        private final String distanceFunctionId;

        @NonNull
        @Builder.Default
        private final Map<String, String> executionParams = new HashMap<>();

        public String getDistanceFunctionName() {
            return executionParams.getOrDefault("distanceFunctionName", DistanceFunction.PredefinedNames.NONE);
        }
    }

    @Value(staticConstructor = "of")
    public static class StopRequest {

        @NonNull
        private final String executionId;
    }

    @Value(staticConstructor = "of")
    public static class StatusRequest {

        @NonNull
        private final String executionId;
    }

}
