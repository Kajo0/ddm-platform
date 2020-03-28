package pl.edu.pw.ddm.platform.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
class AppConfig {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
