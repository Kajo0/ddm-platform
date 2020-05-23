package pl.edu.pw.ddm.platform.agent.configuration;

import java.net.InetAddress;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.pw.ddm.platform.agent.configuration.dto.ConfigurationDto;

@Slf4j
@Service
class DefaultConfigurationProvider implements ConfigurationProvider {

    @SneakyThrows
    @Override
    public ConfigurationDto info() {
        String localhost = InetAddress.getLocalHost().getHostName();
        log.info("Info localhost name: " + localhost);

        return ConfigurationDto.builder()
                .localHostName(localhost)
                .build();
    }

}
