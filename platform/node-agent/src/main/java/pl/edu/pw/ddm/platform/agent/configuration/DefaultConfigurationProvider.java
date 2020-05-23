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
        String ip = InetAddress.getLocalHost().getHostAddress();
        log.info("Info localhost name: " + localhost);
        log.info("Info localhost ip: " + ip);

        return ConfigurationDto.builder()
                .localHostName(localhost)
                .localHostIp(ip)
                .build();
    }

}
