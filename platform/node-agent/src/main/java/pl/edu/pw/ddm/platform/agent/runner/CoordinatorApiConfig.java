package pl.edu.pw.ddm.platform.agent.runner;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "coordinator.api")
class CoordinatorApiConfig {

    private String host;
    private String port;
    private String addresses;

    public String getBaseUrl() {
        return Stream.of(host, port)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(":"));
    }

}
