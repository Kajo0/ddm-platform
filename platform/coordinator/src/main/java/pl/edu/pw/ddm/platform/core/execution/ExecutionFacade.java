package pl.edu.pw.ddm.platform.core.execution;

import java.util.List;
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
    private final ResultsCollector resultsCollector;

    public String start(@NonNull StartRequest request) {
        InstanceAddrDto masterAddr = findMasterAddress(request.instanceId);

        String distanceFunctionId;
        String distanceFunctionName;
        if (DistanceFunction.PREDEFINED_FUNCTIONS.contains(request.distanceFunctionIdOrPredefinedName)) {
            distanceFunctionId = null;
            distanceFunctionName = request.distanceFunctionIdOrPredefinedName;
        } else {
            var req = DistanceFunctionFacade.DescriptionRequest.of(request.distanceFunctionIdOrPredefinedName);
            DistanceFunctionDescDto desc = distanceFunctionFacade.description(req);
            distanceFunctionId = request.distanceFunctionIdOrPredefinedName;
            distanceFunctionName = desc.getFunctionName();
        }

        return executionStarter.start(masterAddr, request.instanceId, request.algorithmId, request.dataId, distanceFunctionId, distanceFunctionName);
    }

    public String stop(@NonNull StopRequest request) {
        return executionStarter.stop(request.getExecutionId());
    }

    public ExecutionDescDto status(@NonNull StatusRequest request) {
        return Optional.of(request)
                .map(StatusRequest::getExecutionId)
                .map(executionStarter::status)
                .map(ExecutionStatusMapper.INSTANCE::map)
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

    public String collectResults(@NonNull CollectResultsRequest request) {
        ExecutionStarter.ExecutionDesc desc = executionStarter.status(request.executionId);
        var req = InstanceFacade.AddressRequest.of(desc.getInstanceId());
        List<InstanceAddrDto> addresses = instanceFacade.addresses(req);

        return resultsCollector.collect(addresses, desc);
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
        private final String dataId;

        @NonNull
        @Builder.Default
        private final String distanceFunctionIdOrPredefinedName = DistanceFunction.PredefinedNames.NONE;
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

    @Value(staticConstructor = "of")
    public static class CollectResultsRequest {

        @NonNull
        private final String executionId;
    }

}
