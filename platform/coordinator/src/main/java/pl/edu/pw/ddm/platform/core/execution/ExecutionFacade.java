package pl.edu.pw.ddm.platform.core.execution;

import java.net.InetAddress;
import java.net.UnknownHostException;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;
import pl.edu.pw.ddm.platform.core.instance.InstanceFacade;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

@Slf4j
@Service
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ExecutionFacade {

    private final InstanceFacade instanceFacade;
    private final ExecutionStarter executionStarter;
    private final Environment env;

    public String start(@NonNull StartRequest request) {
        var req = InstanceFacade.AddressRequest.builder()
                .instanceId(request.instanceId)
                .build();
        InstanceAddrDto masterAddr = instanceFacade.addresses(req)
                .stream()
                .filter(addressDto -> "master".equals(addressDto.getType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No master node for instance: " + request.instanceId));

        // TODO debug - remove on release
        if (env.acceptsProfiles(Profiles.of("localmaster"))) {
            try {
                masterAddr.setAddress(InetAddress.getLocalHost().getHostAddress());
                masterAddr.setAgentPort("7100");
            } catch (UnknownHostException e) {
                log.error("Getting localhost address error.", e);
            }
        }

        return executionStarter.start(masterAddr, request.instanceId, request.algorithmId, request.dataId);
    }

    @Builder
    public static class StartRequest {

        @NonNull
        private final String instanceId;

        @NonNull
        private final String algorithmId;

        @NonNull
        private final String dataId;
    }

}
