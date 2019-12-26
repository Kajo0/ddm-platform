package pl.edu.pw.ddm.platform.core.instance;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Value;
import org.springframework.stereotype.Service;

@Service
class InstanceConfig {

    @Getter
    private final Map<String, InstanceData> instanceMap = new HashMap<>();

    InstanceData add(InstanceData data) {
        return instanceMap.put(data.id, data);
    }

    InstanceData get(String id) {
        return instanceMap.get(id);
    }

    InstanceData remove(String id) {
        return instanceMap.remove(id);
    }

    @Value
    static class InstanceData {

        private String id;
        private Map<String, InstanceNode> nodes;
    }

    @Value
    static class InstanceNode {

        private String id;
        private String type;
        private String address;
        private String port;
    }

}
