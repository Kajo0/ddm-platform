package pl.edu.pw.ddm.platform.core.instance;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;
import pl.edu.pw.ddm.platform.core.util.ProfileConstants;

@Slf4j
@Service
@AllArgsConstructor
public class InstanceFacade {

    private final InstanceConfig instanceConfig;
    private final InstanceCreator creator;
    private final Environment env;

    public String create(@NonNull CreateRequest request) {
        return creator.create(request.workerNodes);
    }

    public String destroy(@NonNull DestroyRequest request) {
        return creator.destroy(request.instanceId) ? "ok" : "nook";
    }

    public String destroyAll() {
        creator.destroyAll();
        return "ok";
    }

    public List<InstanceAddrDto> addresses(@NonNull AddressRequest request) {
        // TODO NPE fix add checks somewhere
        List<InstanceAddrDto> addr = instanceConfig.get(request.instanceId)
                .getNodes()
                .values()
                .stream()
                .map(InstanceConfigMapper.INSTANCE::map)
                .collect(Collectors.toUnmodifiableList());

        // TODO debug - remove on release
        if (env.acceptsProfiles(Profiles.of(ProfileConstants.LOCAL_MASTER))) {
            InstanceAddrDto masterAddr = addr.stream()
                    .filter(InstanceAddrDto::isMaster)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No master node found for instance: " + request.instanceId));
            try {
                masterAddr.setAddress(InetAddress.getLocalHost().getHostAddress());
                masterAddr.setAgentPort("7100");
            } catch (UnknownHostException e) {
                log.error("Getting localhost address error.", e);
            }
        }

        return addr;
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

    @Value(staticConstructor = "of")
    public static class DestroyRequest {

        @NonNull
        private final String instanceId;
    }

    @Value(staticConstructor = "of")
    public static class AddressRequest {

        @NonNull
        private final String instanceId;
    }

}
