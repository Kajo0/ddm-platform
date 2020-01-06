package pl.edu.pw.ddm.platform.core.coordinator;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.ddm.platform.core.instance.InstanceFacade;

@RestController
@RequestMapping("coordinator/command")
class InstanceCommandController {

    private final InstanceFacade instanceFacade;

    InstanceCommandController(InstanceFacade instanceFacade) {
        this.instanceFacade = instanceFacade;
    }

    @GetMapping("instance/create/{workers}")
    String createInstance(@PathVariable Integer workers) {
        // TODO advance parametrization
        var req = InstanceFacade.CreateRequest.builder()
                .ddmModel("central")
                .workerNodes(workers)
                .build();
        return instanceFacade.create(req);
    }

    @GetMapping("instance/destroy/{instanceId}")
    String destroyInstance(@PathVariable String instanceId) {
        var req = InstanceFacade.DestroyRequest.builder()
                .instanceId(instanceId)
                .build();
        return instanceFacade.destroy(req);
    }

    @GetMapping("instance/info")
    String instanceInfo() {
        return instanceFacade.info();
    }

}
