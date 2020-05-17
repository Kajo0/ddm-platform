package pl.edu.pw.ddm.platform.core.coordinator;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.ddm.platform.core.instance.InstanceFacade;
import pl.edu.pw.ddm.platform.core.util.ProfileConstants;

@RestController
@RequestMapping("coordinator/command/instance")
@Profile(ProfileConstants.INSTANCE_LOCAL_DOCKER)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class InstanceLocalDockerCommandController {

    private final InstanceFacade instanceFacade;

    @PostMapping("create/{workers}")
    String createInstance(@PathVariable Integer workers,
                          @RequestParam(value = "cpu", required = false) Integer cpu,
                          @RequestParam(value = "memory", required = false) Integer memory,
                          @RequestParam(value = "disk", required = false) Integer disk) {
        var req = InstanceFacade.CreateRequest.builder()
                .ddmModel("central")
                .workerNodes(workers)
                .cpuCount(cpu)
                .memoryInGb(memory)
                .diskInGb(disk)
                .build();
        return instanceFacade.create(req);
    }

    @GetMapping("destroy/{instanceId}")
    String destroyInstance(@PathVariable String instanceId) {
        var req = InstanceFacade.DestroyRequest.of(instanceId);
        return instanceFacade.destroy(req);
    }

    @GetMapping("destroy/all")
    String destroyAll() {
        return instanceFacade.destroyAll();
    }

}
