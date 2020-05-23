package pl.edu.pw.ddm.platform.core.instance;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class StatusProvider {

    private final RestTemplate restTemplate;

    boolean checkStatus(InstanceConfig.InstanceNode node) {
        var addr = InstanceConfigMapper.INSTANCE.map(node);
        var url = InstanceAgentAddressFactory.healthCheckStatus(addr);

        log.debug("Checking health for: '{}'.", addr);
        var response = restTemplate.getForEntity(url, Void.class);
        log.info("Health of node status by code: '{}'", response.getStatusCodeValue());

        return response.getStatusCode() == HttpStatus.OK;
    }

    NodeConfiguration collectConfig(InstanceConfig.InstanceNode node) {
        var addr = InstanceConfigMapper.INSTANCE.map(node);
        var url = InstanceAgentAddressFactory.collectConfig(addr);

        log.debug("Collecting config from: '{}'.", addr);
        var response = restTemplate.getForObject(url, NodeConfiguration.class);
        log.info("Collected config for node: '{}'", response);

        return response;
    }

    @Data
    static class NodeConfiguration {

        private String localHostName;
    }

}
