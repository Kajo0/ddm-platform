package pl.edu.pw.ddm.api.core.instance;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.ddm.api.core.instance.dto.ProcessDataDto;

@Slf4j
@RestController
@RequestMapping("instance")
class InstanceController {

    @GetMapping("create/{type}")
    ProcessDataDto create(@PathVariable String type) {
        log.debug("[TMP] creating " + type);
        return new ProcessDataDto(UUID.randomUUID().toString(), ProcessType.Instance.CREATE);
    }

    @GetMapping("{instanceId}/init/{model}/{nodes}")
    ProcessDataDto init(@PathVariable String instanceId, @PathVariable String model, @PathVariable int nodes) {
        log.debug("[TMP] init " + model + " model on " + nodes + " nodes");
        return new ProcessDataDto(instanceId, ProcessType.Instance.INIT);
    }

    @GetMapping("{instanceId}/destroy")
    ProcessDataDto destroy(@PathVariable String instanceId) {
        log.debug("[TMP] destroying " + instanceId);
        return new ProcessDataDto(instanceId, ProcessType.Instance.DESTROY);
    }

}
