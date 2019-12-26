package pl.edu.pw.ddm.api.core.instance;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.ddm.api.core.instance.dto.ProcessDataDto;

@Slf4j
@RestController
@RequestMapping("instance/{instanceId}/data")
class DataController {

    @PostMapping("load")
    ProcessDataDto load(@RequestParam String uri) {
        log.debug("[TMP] loading data from " + uri);
        return ProcessDataDto.dummy(ProcessType.Data.DATA_LOAD);
    }

    @PostMapping("scatter/{strategy}")
    ProcessDataDto scatter(@PathVariable String strategy, @RequestBody ScatterParams params) {
        log.debug("[TMP] scattering data " + strategy + " with params: " + params);
        return ProcessDataDto.dummy(ProcessType.Data.DATA_SCATTER);
    }

    @Data
    private static class ScatterParams {}

}
