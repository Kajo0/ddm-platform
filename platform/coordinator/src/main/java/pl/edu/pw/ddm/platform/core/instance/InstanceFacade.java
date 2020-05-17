package pl.edu.pw.ddm.platform.core.instance;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;
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

    public String setup(@NonNull SetupRequest request) {
        ManualInstanceSetupValidator validator = new ManualInstanceSetupValidator(request);
        validator.validate();

        var data = validator.toInstanceData();
        log.info("Setup instance config: '{}'.", data);
        instanceConfig.add(data);

        return data.getId();
    }

    public String create(@NonNull CreateRequest request) {
        return creator.create(request.workerNodes, request.cpuCount, request.memoryInGb, request.diskInGb);
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

    @Getter
    @Builder
    public static class SetupRequest {

        @NonNull
        private final String ddmModel;

        @Singular("node")
        private final List<ManualSetupNode> nodes;

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ManualSetupNode {

            private String name;

            @NonNull
            private String type;

            @NonNull
            private String address;

            @NonNull
            private String port;

            private String uiPort;

            @NonNull
            private String agentPort;

            @NonNull
            private Integer cpu;

            @NonNull
            private Integer memoryInGb;
        }

    }

    @Builder
    public static class CreateRequest {

        @NonNull
        private final String ddmModel;

        @NonNull
        private final Integer workerNodes;

        private final Integer cpuCount;
        private final Integer memoryInGb;
        private final Integer diskInGb;
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
