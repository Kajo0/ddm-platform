package pl.edu.pw.ddm.platform.core.instance;

import javax.annotation.PreDestroy;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class LocalInstanceCreator implements InstanceCreator {

    private static final Long GB_BYTE_MULTIPLIER = 1024 * 1024 * 1024L;

    private final InstanceConfig instanceConfig;
    private final String coordinatorPort;
    private final String nodeAgentPort;
    private final String sparkMasterPort;
    private final String sparkMasterUiPort;
    private final String sparkWorkerUiPort;

    LocalInstanceCreator(InstanceConfig instanceConfig,
                         @Value("${server.port}") String serverPort,
                         @Value("${communiaction.node-agent-port}") String nodeAgentPort,
                         @Value("${communiaction.spark.master-port}") String sparkMasterPort,
                         @Value("${communiaction.spark.master-ui-port}") String sparkMasterUiPort,
                         @Value("${communiaction.spark.worker-ui-port}") String sparkWorkerUiPort) {
        this.instanceConfig = instanceConfig;
        this.coordinatorPort = serverPort;
        this.nodeAgentPort = nodeAgentPort;
        this.sparkMasterPort = sparkMasterPort;
        this.sparkMasterUiPort = sparkMasterUiPort;
        this.sparkWorkerUiPort = sparkWorkerUiPort;
    }

    @Override
    public String create(int workers, Integer cpuCores, Integer memoryInGb, Integer diskInGb) {
        var instanceId = UUID.randomUUID()
                .toString();
        var client = DockerClientBuilder.getInstance()
                .build();

        // Create network
        var networkName = "platform-network-" + instanceId;
        client.createNetworkCmd()
                .withName(networkName)
                .exec();

        // Create master node
        var uiPort = findOpenPort().toString();
        var masterPort = findOpenPort().toString();
        var agentPort = findOpenPort().toString();
        var masterName = "platform-spark-master-" + instanceId;
        var hc = new HostConfig()
                .withPortBindings(PortBinding.parse(uiPort + ":" + sparkMasterUiPort), PortBinding.parse(masterPort + ":" + sparkMasterPort), PortBinding.parse(agentPort + ":" + nodeAgentPort))
                .withNetworkMode(networkName);
        if (cpuCores != null) {
            hc.withCpuCount(cpuCores.longValue());
        }
        if (memoryInGb != null) {
            hc.withMemory(memoryInGb.longValue() * GB_BYTE_MULTIPLIER);
        }
        if (diskInGb != null) {
            hc.withDiskQuota(diskInGb.longValue() * GB_BYTE_MULTIPLIER);
        }

        log.debug("Creating master '{}'.", masterName);
        var masterContainer = client.createContainerCmd("ddm-platform-master")
                .withName(masterName)
                .withEnv(prepareMasterEnv(cpuCores, memoryInGb))
                .withHostConfig(hc)
                .exec();
        // FIXME clean on error
        log.debug("Starting master '{}' with id '{}'.", masterName, masterContainer.getId());
        client.startContainerCmd(masterContainer.getId())
                .exec();

        var masterId = masterContainer.getId();
        var nodes = new HashMap<String, InstanceConfig.InstanceNode>();
        nodes.put(masterId, new InstanceConfig.InstanceNode(masterId, masterName, "master", "localhost", masterPort, uiPort, agentPort, cpuCores, memoryInGb, diskInGb));

        // Create worker nodes
        for (int i = 1; i <= workers; ++i) {
            var port = findOpenPort().toString();
            var workerAgentPort = findOpenPort().toString();
            var hcw = new HostConfig()
                    .withPortBindings(PortBinding.parse(port + ":" + sparkWorkerUiPort), PortBinding.parse(workerAgentPort + ":" + nodeAgentPort))
                    .withNetworkMode(networkName);
            if (cpuCores != null) {
                hcw.withCpuCount(cpuCores.longValue());
            }
            if (memoryInGb != null) {
                hcw.withMemory(memoryInGb.longValue() * GB_BYTE_MULTIPLIER);
            }
            if (diskInGb != null) {
                hcw.withDiskQuota(diskInGb.longValue() * GB_BYTE_MULTIPLIER);
            }

            var containerName = "platform-spark-worker-" + i + "-" + instanceId;
            log.debug("Creating worker '{}'.", containerName);
            var container = client.createContainerCmd("ddm-platform-worker")
                    .withName(containerName)
                    .withEnv(prepareWorkerEnv(masterName, cpuCores, memoryInGb))
                    .withHostConfig(hcw)
                    .exec();
            // FIXME clean on error
            log.debug("Starting worker '{}' with id '{}'.", containerName, container.getId());
            client.startContainerCmd(container.getId())
                    .exec();

            var workerId = container.getId();
            nodes.put(workerId, new InstanceConfig.InstanceNode(workerId, containerName, "worker", "localhost", port, null, workerAgentPort, cpuCores, memoryInGb, diskInGb));
        }

        var data = new InstanceConfig.InstanceData(instanceId, networkName, nodes);
        instanceConfig.add(data);
        return instanceId;
    }

    @Override
    public boolean destroy(String id) {
        var instance = instanceConfig.get(id);
        if (instance == null) {
            throw new IllegalArgumentException("No instance with id " + id + " to destroy.");
        }

        var client = DockerClientBuilder.getInstance()
                .build();
        // FIXME first workers, then master
        instance.getNodes()
                .values()
                .parallelStream()
                .map(InstanceConfig.InstanceNode::getId)
                .forEach(containerId -> {
                    log.debug("Stopping container: '{}'.", containerId);
                    client.stopContainerCmd(containerId)
                            .exec();
                    log.debug("Removing container: '{}'.", containerId);
                    client.removeContainerCmd(containerId)
                            .exec();
                });

        log.debug("Removing network: '{}'.", instance.getNetworkName());
        client.removeNetworkCmd(instance.getNetworkName())
                .exec();

        return instanceConfig.remove(id) != null;
    }

    @Override
    public void destroyAll() {
        instanceConfig.getInstanceMap()
                .keySet()
                .parallelStream()
                .forEach(this::destroy);
    }

    @PreDestroy
    void destroy() {
        // TODO disable for persistent config
        log.info("PreDestroy " + this.getClass()
                .getSimpleName());

        destroyAll();
    }

    @SneakyThrows
    private Integer findOpenPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private List<String> prepareMasterEnv(Integer cpuCount, Integer memoryInGb) {
        List<String> envs = new ArrayList<>();
        envs.add("INIT_DAEMON_STEP=setup_spark");
        envs.add("COORDINATOR_HOST=");
        envs.add("COORDINATOR_PORT=" + coordinatorPort);

        if (cpuCount != null) {
            log.info("Setting master cores count={}.", cpuCount);
            envs.add("SPARK_WORKER_CORES=" + cpuCount);
            envs.add("SPARK_EXECUTOR_CORES=" + cpuCount);
        }
        if (memoryInGb != null) {
            log.info("Setting master memory in gb={}.", memoryInGb);
            envs.add("SPARK_DRIVER_MEMORY=" + memoryInGb + "G");
            envs.add("SPARK_EXECUTOR_MEMORY=" + memoryInGb + "G");
            envs.add("SPARK_WORKER_MEMORY=" + memoryInGb + "g");
        }

        return envs;
    }

    private List<String> prepareWorkerEnv(String masterName, Integer cpuCount, Integer memoryInGb) {
        List<String> envs = new ArrayList<>();
        envs.add("SPARK_MASTER=spark://" + masterName + ":" + sparkMasterPort);
        envs.add("COORDINATOR_HOST=");
        envs.add("COORDINATOR_PORT=" + coordinatorPort);

        if (cpuCount != null) {
            log.info("Setting worker cores count={}.", cpuCount);
            envs.add("SPARK_WORKER_CORES=" + cpuCount);
            envs.add("SPARK_EXECUTOR_CORES=" + cpuCount);
        }
        if (memoryInGb != null) {
            log.info("Setting worker memory in gb={}.", memoryInGb);
            envs.add("SPARK_DRIVER_MEMORY=" + memoryInGb + "G");
            envs.add("SPARK_EXECUTOR_MEMORY=" + memoryInGb + "G");
            envs.add("SPARK_WORKER_MEMORY=" + memoryInGb + "g");
        }

        return envs;
    }

}
