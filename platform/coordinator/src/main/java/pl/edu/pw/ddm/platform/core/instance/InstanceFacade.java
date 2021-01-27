package pl.edu.pw.ddm.platform.core.instance;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class InstanceFacade {

    private final InstanceConfig instanceConfig;
    private final InstanceCreator creator;
    private final StatusProvider statusProvider;
    private final SetupUpdater setupUpdater;
    private final Environment env;

    @org.springframework.beans.factory.annotation.Value("${communiaction.node-agent-port}")
    private String nodeAgentPort;

    public String setup(@NonNull SetupRequest request) {
        ManualInstanceSetupValidator validator = new ManualInstanceSetupValidator(request);
        validator.validate();

        var data = validator.toInstanceData();
        log.info("Setup instance config: '{}'.", data);
        instanceConfig.add(data);

        return data.getId();
    }

    public String create(@NonNull CreateRequest request) {
        return creator.create(request.workerNodes, request.cpuCount, request.workerMemoryInGb, request.masterMemoryInGb,
                request.diskInGb);
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
                masterAddr.setAgentPort(nodeAgentPort);
            } catch (UnknownHostException e) {
                log.error("Getting localhost address error.", e);
            }
        }

        return addr;
    }

    public boolean status(@NonNull StatusRequest request) {
        return instanceConfig.get(request.instanceId)
                .getNodes()
                .values()
                .stream()
                .allMatch(n -> {
                    boolean status = statusProvider.checkStatus(n);
                    instanceConfig.updateAlive(request.instanceId, n.getId(), status);
                    return status;
                });
    }

    public boolean updateConfig(@NonNull UpdateConfigRequest request) {
        var instance = instanceConfig.get(request.instanceId);
        var nodes = instance.getNodes()
                .values();

        nodes.forEach(n -> {
            var config = statusProvider.collectConfig(n);
            instanceConfig.updateLocalhostName(request.instanceId, n.getId(), config.getLocalHostName());
            instanceConfig.updateLocalhostIp(request.instanceId, n.getId(), config.getLocalHostIp());
        });

        return nodes.stream()
                .allMatch(node -> setupUpdater.updateSetup(instance, node));
    }

    public boolean updateAlgorithmScatter(@NonNull InstanceFacade.AlgorithmScatteredRequest request) {
        var instance = instanceConfig.get(request.instanceId);
        Preconditions.checkNotNull(instance, "No instance with id '%s'.", request.instanceId);

        var inserted = instanceConfig.updateAlgorithm(request.instanceId, request.algorithmId);
        if (inserted) {
            log.info("Algorithm '{}' scatter info inserted for instance '{}'.", request.instanceId,
                    request.algorithmId);
        } else {
            log.info("Algorithm '{}' scatter info already present for instance '{}'.", request.instanceId,
                    request.algorithmId);
        }
        return inserted;
    }

    public void algorithmUpdate(String algorithmId) {
        log.info("Clearing algorithm '{}' from config.", algorithmId);
        instanceConfig.clearAlgorithm(algorithmId);
    }

    public boolean updateDataScatter(@NonNull DataScatteredRequest request) {
        var instance = instanceConfig.get(request.instanceId);
        Preconditions.checkNotNull(instance, "No instance with id '%s'.", request.instanceId);

        var updated = instanceConfig.updateData(
                request.instanceId,
                request.dataId,
                request.strategyName,
                request.strategyParams,
                request.distanceFunction,
                request.seed
        );
        if (updated) {
            log.info("Data '{}' scatter info already present for instance '{}'.", request.instanceId,
                    request.dataId);
        } else {
            log.info("Data '{}' scatter info inserted for instance '{}'.", request.instanceId,
                    request.dataId);
        }
        return updated;
    }

    // TODO think about structured response
    public String dataScatter(@NonNull DataScatterRequest request) {
        var info = instanceConfig.get(request.instanceId)
                .getInfo()
                .getDataScatter()
                .get(request.dataId);
        try {
            return new ObjectMapper().writeValueAsString(info);
        } catch (JsonProcessingException e) {
            return String.valueOf(info);
        }
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
        private final Integer workerMemoryInGb;
        private final Integer masterMemoryInGb;
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

    @Value(staticConstructor = "of")
    public static class StatusRequest {

        @NonNull
        private final String instanceId;
    }

    @Value(staticConstructor = "of")
    public static class UpdateConfigRequest {

        @NonNull
        private final String instanceId;
    }

    @Value(staticConstructor = "of")
    public static class AlgorithmScatteredRequest {

        @NonNull
        private final String instanceId;

        @NonNull
        private final String algorithmId;
    }

    @Builder
    public static class DataScatteredRequest {

        @NonNull
        private final String instanceId;

        @NonNull
        private final String dataId;

        private final String strategyName;
        private final String strategyParams;
        private final String distanceFunction;
        private final Long seed;
    }

    @Value(staticConstructor = "of")
    public static class DataScatterRequest {

        @NonNull
        private final String instanceId;

        @NonNull
        private final String dataId;
    }

}
