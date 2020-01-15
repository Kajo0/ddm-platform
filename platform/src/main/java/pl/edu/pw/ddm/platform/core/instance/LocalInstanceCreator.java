package pl.edu.pw.ddm.platform.core.instance;

import javax.annotation.PreDestroy;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
        var masterName = "platform-spark-master-" + instanceId;
        var hc = new HostConfig().withPortBindings(PortBinding.parse(uiPort + ":8080"),
                PortBinding.parse(masterPort + ":7077"))
                .withNetworkMode(networkName);
        log.debug("Creating master '{}'.", masterName);
        var masterContainer = client.createContainerCmd("bde2020/spark-master:2.4.4-hadoop2.7")
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
        nodes.put(masterId, new InstanceConfig.InstanceNode(masterId, masterName, "master", "localhost", masterPort));

        // Create worker nodes
        for (int i = 1; i <= workers; ++i) {
            var port = findOpenPort().toString();
            var hcw = new HostConfig().withPortBindings(PortBinding.parse(port + ":8081"))
                    .withNetworkMode(networkName);

            var containerName = "platform-spark-worker-" + i + "-" + instanceId;
            log.debug("Creating worker '{}'.", containerName);
            var container = client.createContainerCmd("bde2020/spark-worker:2.4.4-hadoop2.7")
                    .withName(containerName)
                    .withEnv("SPARK_MASTER=spark://" + masterName + ":" + 7077)
                    .withHostConfig(hcw)
                    .exec();
            // FIXME clean on error
            log.debug("Starting worker '{}' with id '{}'.", containerName, container.getId());
            client.startContainerCmd(container.getId())
                    .exec();

            var workerId = container.getId();
            nodes.put(workerId, new InstanceConfig.InstanceNode(workerId, containerName, "worker", "localhost", port));
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
                .forEach(node -> {
                    log.info("Stopping container '{}'.", node);
                    client.stopContainerCmd(node.getId())
                            .exec();
                });

        return instanceConfig.remove(id) != null;
    }

    @PreDestroy
    void destroy() {
        // TODO disable for persistent config
        log.info("PreDestroy " + this.getClass()
                .getSimpleName());
        var client = DockerClientBuilder.getInstance()
                .build();

        instanceConfig.getInstanceMap()
                .values()
                .stream()
                .map(InstanceConfig.InstanceData::getNodes)
                .map(Map::values)
                .flatMap(Collection::parallelStream)
                .map(InstanceConfig.InstanceNode::getId)
                .map(client::stopContainerCmd)
                .forEach(cmd -> {
                    log.debug("Stopping container: '{}'.", cmd.getContainerId());
                    cmd.exec();
                });
    }

    @SneakyThrows
    private Integer findOpenPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

}
