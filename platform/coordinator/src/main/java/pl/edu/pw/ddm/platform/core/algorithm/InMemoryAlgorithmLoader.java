package pl.edu.pw.ddm.platform.core.algorithm;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class InMemoryAlgorithmLoader implements AlgorithmLoader {

    private final Map<String, AlgorithmDesc> algorithmMap = new HashMap<>();

    @Override
    public String load(String url) {
        throw new NotImplementedException("Not yet implemented.");
    }

    @Override
    public String load(String name, byte[] jar) {
        var id = UUID.nameUUIDFromBytes((name + "/" + jar.length).getBytes())
                .toString();
        var alg = new AlgorithmDesc(id, name, jar);

        if (algorithmMap.putIfAbsent(id, alg) != null) {
            log.warn("Loaded the same jar '{}' file as before with id '{}'.", name, id);
        }
        return id;
    }

    @Override
    public AlgorithmDesc getAlgorithm(String algorithmId) {
        return algorithmMap.get(algorithmId);
    }

    @PreDestroy
    void destroy() {
        // TODO disable for persistent config
        log.info("PreDestroy " + this.getClass()
                .getSimpleName());
        algorithmMap.values()
                .parallelStream()
                .map(AlgorithmDesc::getName)
                .forEach(name -> {
                    log.debug("Deleting algorithm file: '{}'.", name);
                });
    }

    @Value
    static class AlgorithmDesc {

        private String id;
        private String name;
        private byte[] jar;
    }

}
