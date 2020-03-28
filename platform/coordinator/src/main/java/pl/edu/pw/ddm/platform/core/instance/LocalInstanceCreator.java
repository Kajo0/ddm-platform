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
import org.springframework.stereotype.Service;

@Slf4j
@Service
class LocalInstanceCreator implements InstanceCreator {

    private final InstanceConfig instanceConfig;

    LocalInstanceCreator(InstanceConfig instanceConfig) {
        this.instanceConfig = instanceConfig;
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
        var hc = new HostConfig().withPortBindings(PortBinding.parse(uiPort + ":8080"),
                PortBinding.parse(masterPort + ":7077"), PortBinding.parse(agentPort + ":7100"))
                .withNetworkMode(networkName);
        log.debug("Creating master '{}'.", masterName);
        var masterContainer = client.createContainerCmd("ddm-platform-master")
                .withName(masterName)
                .withEnv("INIT_DAEMON_STEP=setup_spark")
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
            var hcw = new HostConfig().withPortBindings(PortBinding.parse(port + ":8081"),
                    PortBinding.parse(workerAgentPort + ":7100"))
                    .withNetworkMode(networkName);

            var containerName = "platform-spark-worker-" + i + "-" + instanceId;
            log.debug("Creating worker '{}'.", containerName);
            var container = client.createContainerCmd("ddm-platform-worker")
                    .withName(containerName)
                    .withEnv("SPARK_MASTER=spark://" + masterName + ":" + 7077)
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
