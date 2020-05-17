package pl.edu.pw.ddm.platform.core.coordinator;

import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.ddm.platform.core.instance.InstanceFacade;
import pl.edu.pw.ddm.platform.core.util.ProfileConstants;

@RestController
@RequestMapping("coordinator/command/instance")
@Profile(ProfileConstants.INSTANCE_MANUAL_SETUP)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class InstanceManualSetupCommandController {

    private final InstanceFacade instanceFacade;

    @PostMapping("setup/manual")
    String setupInstance(@RequestBody ManualSetup setup) {
        var req = InstanceFacade.SetupRequest.builder()
                .ddmModel("central");
        setup.getNodes()
                .stream()
                .map(InstanceDataMapper.INSTANCE::map)
                .forEach(req::node);

        return instanceFacade.setup(req.build());
    }

    // TODO think about update / destroy previous configs

    @Data
    static class ManualSetup {

        List<ManualSetupNode> nodes;
    }

    @Data
    static class ManualSetupNode {

        private String name;

        @NonNull
        private String type;

        @NonNull
        private String address;

        @NonNull
        private String port;

        private String uiPort;

        @NonNull
        private String agentPort;

        @NonNull
        private Integer cpu;

        @NonNull
        private Integer memoryInGb;
    }

}
