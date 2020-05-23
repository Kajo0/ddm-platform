package pl.edu.pw.ddm.platform.core.instance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

@Service
class InstanceConfig {

    @Getter
    private final Map<String, InstanceData> instanceMap = new ConcurrentHashMap<>();

    InstanceData add(InstanceData data) {
        return instanceMap.put(data.id, data);
    }

    InstanceData get(String id) {
        return instanceMap.get(id);
    }

    InstanceData remove(String id) {
        return instanceMap.remove(id);
    }

    boolean updateAlive(String id, String nodeId, boolean alive) {
        InstanceNode node = node(id, nodeId);
        boolean previous = node.alive;
        node.alive = alive;

        return previous != alive;
    }

    boolean updateLocalhostName(String id, String nodeId, String name) {
        InstanceNode node = node(id, nodeId);
        String previous = node.localhostName;
        node.localhostName = name;

        return !StringUtils.equals(previous, name);
    }

    boolean updateLocalhostIp(String id, String nodeId, String ip) {
        InstanceNode node = node(id, nodeId);
        String previous = node.localhostIp;
        node.localhostIp = ip;

        return !StringUtils.equals(previous, ip);
    }

    private InstanceNode node(String id, String nodeId) {
        return instanceMap.get(id)
                .nodes
                .get(nodeId);
    }

    @Value
    static class InstanceData {

        private String id;
        private InstanceType type;
        private String networkName;
        private Map<String, InstanceNode> nodes;

        @Override
        public String toString() {
            try {
                return new ObjectMapper().writeValueAsString(this);
            } catch (JsonProcessingException e) {
                return super.toString();
            }
        }
    }

    // TODO make it more immutable and allow update only certain fields
    @Getter
    @AllArgsConstructor
    static class InstanceNode {

        private String id;
        private String containerId;
        private String name;
        private String type;
        private String address;
        private String localhostName;
        private String localhostIp;
        private boolean alive;
        private String port;
        private String uiPort;
        private String agentPort;
        private Integer cpu;
        private Integer memory;
        private Integer disk;

        @Override
        public String toString() {
            try {
                return new ObjectMapper().writeValueAsString(this);
            } catch (JsonProcessingException e) {
                return super.toString();
            }
        }
    }

    enum InstanceType {
        LOCAL_DOCKER,
        MANUAL_SETUP
    }

}
