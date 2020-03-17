package pl.edu.pw.ddm.platform.core.algorithm;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

@Slf4j
@Service
class DefaultAlgorithmBroadcaster implements AlgorithmBroadcaster {

    @Override
    public String broadcast(List<InstanceAddrDto> addresses, String algorithmId, byte[] jar) {
        log.info("Broadcasting algorithm with id '{}' and size '{}' into nodes '{}'.", algorithmId, jar.length,
                addresses);
        return "ok_process-id";
    }

}
