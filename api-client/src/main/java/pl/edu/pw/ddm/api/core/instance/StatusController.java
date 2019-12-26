package pl.edu.pw.ddm.api.core.instance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.ddm.api.core.instance.dto.InstanceConfigDto;
import pl.edu.pw.ddm.api.core.instance.dto.ProcessInfoDto;

@Slf4j
@RestController
@RequestMapping("instance/{instanceId}/status")
class StatusController {

    @GetMapping("{processId}")
    ProcessInfoDto status(@PathVariable String processId) {
        log.debug("[TMP] getting status for process " + processId);
        return ProcessInfoDto.dummy();
    }

    @GetMapping("config")
    InstanceConfigDto config() {
        log.debug("[TMP] getting config for instance");
        return InstanceConfigDto.dummy();
    }

}
