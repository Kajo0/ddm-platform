package pl.edu.pw.ddm.platform.core.algorithm;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;
import pl.edu.pw.ddm.platform.core.algorithm.dto.AlgorithmDescDto;
import pl.edu.pw.ddm.platform.core.instance.InstanceFacade;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;
import pl.edu.pw.ddm.platform.core.util.ProfileConstants;

@Slf4j
@Service
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class AlgorithmFacade {

    private final InstanceFacade instanceFacade;
    private final AlgorithmLoader algorithmLoader;
    private final AlgorithmBroadcaster algorithmBroadcaster;
    private final Environment env;

    public String load(@NonNull LoadRequest request) {
        return algorithmLoader.save(request.name, request.jar);
    }

    public String broadcast(@NonNull BroadcastRequest request) {
        var req = InstanceFacade.AddressRequest.of(request.instanceId);
        var addr = instanceFacade.addresses(req)
                .stream()
                .filter(InstanceAddrDto::isMaster)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No master node for instance."));

        var alg = algorithmLoader.getAlgorithm(request.algorithmId);
        if (alg == null) {
            throw new IllegalArgumentException("No algorithm with id: " + request.algorithmId);
        }

        // TODO debug - remove on release
        if (env.acceptsProfiles(Profiles.of(ProfileConstants.LOCAL_MASTER))) {
            try {
                addr.setAddress(InetAddress.getLocalHost().getHostAddress());
                addr.setAgentPort("7100");
            } catch (UnknownHostException e) {
                log.error("Getting localhost address error.", e);
            }
        }

        return algorithmBroadcaster.broadcast(addr, alg);
    }

    public AlgorithmDescDto description(@NonNull DescriptionRequest request) {
        // TODO more checks
        return Optional.of(request)
                .map(DescriptionRequest::getAlgorithmId)
                .map(algorithmLoader::getAlgorithm)
                .map(AlgorithmDescMapper.INSTANCE::map)
                .get();
    }

    // TODO remove debug
    public String info() {
        try {
            return new ObjectMapper().writeValueAsString(algorithmLoader.allAlgorithmsInfo());
        } catch (JsonProcessingException e) {
            return algorithmLoader.allAlgorithmsInfo()
                    .toString();
        }
    }

    @Builder
    public static class LoadRequest {

        private final String name;

        @NonNull
        private final byte[] jar;
    }

    @Builder
    public static class BroadcastRequest {

        @NonNull
        private final String instanceId;

        @NonNull
        private final String algorithmId;
    }

    @Value(staticConstructor = "of")
    public static class DescriptionRequest {

        @NonNull
        private final String algorithmId;
    }

}
