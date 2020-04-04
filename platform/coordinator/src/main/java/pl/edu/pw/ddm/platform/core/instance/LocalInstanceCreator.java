package pl.edu.pw.ddm.platform.core.instance;

import javax.annotation.PreDestroy;
import java.net.ServerSocket;
import java.util.HashMap;
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

    private static final String SPARK_MASTER_UI_PORT = "8080";
    private static final String SPARK_WORKER_UI_PORT = "8081";
    private static final String SPARK_MASTER_PORT = "7077";
    private static final String NODE_AGENT_PORT = "7100";

    private final InstanceConfig instanceConfig;
    private final String port;

    LocalInstanceCreator(InstanceConfig instanceConfig,
                         @Value("${server.port}") String port) {
        this.instanceConfig = instanceConfig;
        this.port = port;
    }

    @Override
    public String create(int workers) {
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
        var hc = new HostConfig().withPortBindings(PortBinding.parse(uiPort + ":" + SPARK_MASTER_UI_PORT),
                PortBinding.parse(masterPort + ":" + SPARK_MASTER_PORT), PortBinding.parse(agentPort + ":" + NODE_AGENT_PORT))
                .withNetworkMode(networkName);
        log.debug("Creating master '{}'.", masterName);
        var masterContainer = client.createContainerCmd("ddm-platform-master")
                .withName(masterName)
                .withEnv("INIT_DAEMON_STEP=setup_spark", "COORDINATOR_HOST=", "COORDINATOR_PORT=" + port)
                .withHostConfig(hc)
                .exec();
        // FIXME clean on error
        log.debug("Starting master '{}' with id '{}'.", masterName, masterContainer.getId());
        client.startContainerCmd(masterContainer.getId())
                .exec();

        var masterId = masterContainer.getId();
        var nodes = new HashMap<String, InstanceConfig.InstanceNode>();
        nodes.put(masterId, new InstanceConfig.InstanceNode(masterId, masterName, "master", "localhost", masterPort, uiPort, agentPort));

        // Create worker nodes
        for (int i = 1; i <= workers; ++i) {
            var port = findOpenPort().toString();
            var workerAgentPort = findOpenPort().toString();
            var hcw = new HostConfig().withPortBindings(PortBinding.parse(port + ":" + SPARK_WORKER_UI_PORT),
                    PortBinding.parse(workerAgentPort + ":" + NODE_AGENT_PORT))
                    .withNetworkMode(networkName);

            var containerName = "platform-spark-worker-" + i + "-" + instanceId;
            log.debug("Creating worker '{}'.", containerName);
            var container = client.createContainerCmd("ddm-platform-worker")
                    .withName(containerName)
                    .withEnv("SPARK_MASTER=spark://" + masterName + ":" + SPARK_MASTER_PORT, "COORDINATOR_HOST=", "COORDINATOR_PORT=" + port)
                    .withHostConfig(hcw)
                    .exec();
            // FIXME clean on error
            log.debug("Starting worker '{}' with id '{}'.", containerName, container.getId());
            client.startContainerCmd(container.getId())
                    .exec();

            var workerId = container.getId();
            nodes.put(workerId, new InstanceConfig.InstanceNode(workerId, containerName, "worker", "localhost", port, null, workerAgentPort));
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

}
