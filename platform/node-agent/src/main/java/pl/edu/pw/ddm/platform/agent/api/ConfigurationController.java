package pl.edu.pw.ddm.platform.agent.api;

import java.nio.file.AccessDeniedException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.ddm.platform.agent.configuration.ConfigurationProvider;
import pl.edu.pw.ddm.platform.agent.configuration.ConfigurationService;
import pl.edu.pw.ddm.platform.agent.configuration.dto.ConfigurationDto;

@RestController
@RequestMapping("agent/config")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class ConfigurationController {

    private final ConfigurationProvider configurationProvider;
    private final ConfigurationService configurationService;

    @GetMapping("info")
    ConfigurationDto info() {
        return configurationProvider.info();
    }

    @PostMapping("setup")
    ResponseEntity<Void> setup(@RequestParam(value = "masterPublicAddress", required = false) String masterPublicAddr) throws AccessDeniedException {
        configurationService.setup(masterPublicAddr);
        return ResponseEntity.ok().build();
    }

}
