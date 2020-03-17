package pl.edu.pw.ddm.platform.core.instance;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

@Service
public class InstanceFacade {

    private final InstanceConfig instanceConfig;
    private final InstanceCreator creator;

    InstanceFacade(InstanceConfig instanceConfig, InstanceCreator creator) {
        this.instanceConfig = instanceConfig;
        this.creator = creator;
    }

    public String create(@NonNull CreateRequest request) {
        return creator.create(request.workerNodes);
    }

    public String destroy(@NonNull DestroyRequest request) {
        return creator.destroy(request.instanceId) ? "ok" : "nook";
    }

    public List<InstanceAddrDto> addresses(@NonNull AddressRequest request) {
        // TODO NPE fix add checks somewhere
        return instanceConfig.get(request.instanceId)
                .getNodes()
                .values()
                .stream()
                .map(InstanceConfigMapper.INSTANCE::map)
                .collect(Collectors.toUnmodifiableList());
    }

    // TODO remove debug
    public String info() {
        try {
            return new ObjectMapper().writeValueAsString(instanceConfig.getInstanceMap());
        } catch (JsonProcessingException e) {
            return instanceConfig.getInstanceMap()
                    .toString();
        }
    }

    @Builder
    public static class CreateRequest {

        @NonNull
        private final String ddmModel;

        private final Integer workerNodes;
    }

    @Builder
    public static class DestroyRequest {

        @NonNull
        private final String instanceId;
    }

    @Builder
    public static class AddressRequest {

        @NonNull
        private final String instanceId;
    }

}
