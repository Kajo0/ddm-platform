package pl.edu.pw.ddm.platform.core.coordinator;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.ddm.platform.core.instance.InstanceFacade;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

@RestController
@RequestMapping("coordinator/command")
class InstanceCommandController {

    private final InstanceFacade instanceFacade;

    InstanceCommandController(InstanceFacade instanceFacade) {
        this.instanceFacade = instanceFacade;
    }

    @PostMapping("instance/create/{workers}")
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

    @GetMapping("instance/destroy/{instanceId}")
    String destroyInstance(@PathVariable String instanceId) {
        var req = InstanceFacade.DestroyRequest.of(instanceId);
        return instanceFacade.destroy(req);
    }

    @GetMapping("instance/destroy/all")
    String destroyAll() {
        return instanceFacade.destroyAll();
    }

    @GetMapping("instance/info/{instanceId}")
    List<InstanceAddrDto> instanceAddresses(@PathVariable String instanceId) {
        var req = InstanceFacade.AddressRequest.of(instanceId);
        return instanceFacade.addresses(req);
    }

    @GetMapping(value = "instance/info", produces = MediaType.APPLICATION_JSON_VALUE)
    String instanceInfo() {
        return instanceFacade.info();
    }

}
