package pl.edu.pw.ddm.platform.core.data;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pw.ddm.platform.core.data.dto.DistanceFunctionDescDto;
import pl.edu.pw.ddm.platform.core.instance.InstanceFacade;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

@Slf4j
@Service
@AllArgsConstructor
public class DistanceFunctionFacade {

    private final InstanceFacade instanceFacade;
    private final DistanceFunctionLoader distanceFunctionLoader;
    private final DistanceFunctionBroadcaster broadcaster;

    public String load(@NonNull DistanceFunctionFacade.LoadRequest request) {
        return distanceFunctionLoader.save(request.file);
    }

    public String broadcast(@NonNull BroadcastRequest request) {
        var req = InstanceFacade.AddressRequest.of(request.instanceId);
        var addr = instanceFacade.addresses(req)
                .stream()
                .filter(InstanceAddrDto::isMaster)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No master node for instance."));

        var distFunc = distanceFunctionLoader.getDistanceFunctionDesc(request.distanceFunctionId);
        if (distFunc == null) {
            throw new IllegalArgumentException("No distance function with id: " + request.distanceFunctionId);
        }

        return broadcaster.broadcast(addr, distFunc);
    }

    public DistanceFunctionDescDto description(@NonNull DescriptionRequest request) {
        // TODO more checks
        return Optional.of(request)
                .map(DescriptionRequest::getDistanceFunctionId)
                .map(distanceFunctionLoader::getDistanceFunctionDesc)
                .map(DataDescMapper.INSTANCE::map)
                .get();
    }

    // TODO remove debug
    public String info() {
        try {
            return new ObjectMapper().writeValueAsString(distanceFunctionLoader.allDistanceFunctionInfo());
        } catch (JsonProcessingException e) {
            return distanceFunctionLoader.allDistanceFunctionInfo()
                    .toString();
        }
    }

    @Value(staticConstructor = "of")
    public static class LoadRequest {

        @NonNull
        private final MultipartFile file;
    }

    @Builder
    public static class BroadcastRequest {

        @NonNull
        private final String instanceId;

        @NonNull
        private final String distanceFunctionId;
    }

    @Value(staticConstructor = "of")
    public static class DescriptionRequest {

        @NonNull
        private final String distanceFunctionId;
    }

}
