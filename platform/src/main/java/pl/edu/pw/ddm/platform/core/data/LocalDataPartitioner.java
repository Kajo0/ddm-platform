package pl.edu.pw.ddm.platform.core.data;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

@Slf4j
@Service
class LocalDataPartitioner implements DataPartitioner {

    @Override
    public String scatter(List<InstanceAddrDto> addresses, LocalDataLoader.DataDesc data, String strategy) {
        log.info("Scattering data '{}' with strategy '{}' into nodes '{}'.", data, strategy, addresses);
        // TODO scatter data
        return "ok_process-id";
    }

}
