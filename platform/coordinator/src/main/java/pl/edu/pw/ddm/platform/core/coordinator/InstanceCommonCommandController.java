package pl.edu.pw.ddm.platform.core.coordinator;

import java.util.List;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.ddm.platform.core.instance.InstanceFacade;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

@RestController
@RequestMapping("coordinator/command/instance")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class InstanceCommonCommandController {

    private final InstanceFacade instanceFacade;

    @GetMapping("info/{instanceId}")
    List<InstanceAddrDto> instanceAddresses(@PathVariable String instanceId) {
        var req = InstanceFacade.AddressRequest.of(instanceId);
        return instanceFacade.addresses(req);
    }

    @GetMapping(value = "status/{instanceId}")
    ResponseEntity<String> instanceStatus(@PathVariable String instanceId) {
        var req = InstanceFacade.StatusRequest.of(instanceId);
        if (instanceFacade.status(req)) {
            return ResponseEntity.ok("ok");
        } else {
            return ResponseEntity.badRequest().body("not-ok");
        }
    }

    @GetMapping(value = "config/{instanceId}/update")
    ResponseEntity<String> updateInstanceConfig(@PathVariable String instanceId) {
        var req = InstanceFacade.UpdateConfigRequest.of(instanceId);
        if (instanceFacade.updateConfig(req)) {
            return ResponseEntity.ok("ok");
        } else {
            return ResponseEntity.badRequest().body("not-ok");
        }
    }

    @GetMapping(value = "info", produces = MediaType.APPLICATION_JSON_VALUE)
    String instanceInfo() {
        return instanceFacade.info();
    }

}
