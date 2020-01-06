package pl.edu.pw.ddm.platform.core.instance;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Value
    static class InstanceNode {

        private String id;
        private String type;
        private String address;
        private String port;

        @Override
        public String toString() {
            try {
                return new ObjectMapper().writeValueAsString(this);
            } catch (JsonProcessingException e) {
                return super.toString();
            }
        }
    }

}
