package pl.edu.pw.ddm.platform.core.coordinator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.ddm.platform.core.instance.InstanceFacade;

@Slf4j
@RestController
@RequestMapping("coordinator/command")
class CommandController {

    private final InstanceFacade instanceFacade;

    CommandController(InstanceFacade instanceFacade) {
        this.instanceFacade = instanceFacade;
    }

    @GetMapping("instance/create")
    String createInstance() {
        log.debug("[TMP] creating instance");
        return instanceFacade.create(null);
    }

    @GetMapping("instance/destroy/{instanceId}")
    void destroyInstance(@PathVariable String instanceId) {
        log.debug("[TMP] destroying instance");
        instanceFacade.destroy(instanceId);
    }

    @GetMapping("instance/info")
    String instanceInfo() {
        log.debug("[TMP] instance info");
        return instanceFacade.info();
    }

}
