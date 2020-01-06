package pl.edu.pw.ddm.platform.core.instance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.stereotype.Service;

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

    public void destroy(@NonNull DestroyRequest request) {
        creator.destroy(request.instanceId);
    }

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

}
