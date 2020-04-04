package pl.edu.pw.ddm.platform.agent.runner;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "apps")
class AppAlgorithmsConfig {

    private RunnerConfig runner;
    private List<String> algorithms = new ArrayList<>();

    @Data
    static class RunnerConfig {

        private String path;
        private String mainClass;
    }

}
