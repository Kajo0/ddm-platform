package pl.edu.pw.ddm.platform.core.instance;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
class LocalInstanceCreator implements InstanceCreator {

    private final InstanceConfig instanceConfig;

    LocalInstanceCreator(InstanceConfig instanceConfig) {
        this.instanceConfig = instanceConfig;
    }

    @Override
    public String create() {
        var id = UUID.randomUUID().toString();
        var masterId = "masterId";
        var workerOne = "worker[1]";
        var workerTwo = "worker[2]";
        var nodes =
                Map.of(masterId, new InstanceConfig.InstanceNode(masterId, "master", "localhost", "5000"), workerOne,
                        new InstanceConfig.InstanceNode(workerOne, "worker", "localhost", "5001"), workerTwo,
                        new InstanceConfig.InstanceNode(workerTwo, "worker", "localhost", "5002"));
        var data = new InstanceConfig.InstanceData(id, nodes);
        instanceConfig.add(data);
        return id;
    }

    @Override
    public boolean destroy(String id) {
        return instanceConfig.remove(id) != null;
    }

}
