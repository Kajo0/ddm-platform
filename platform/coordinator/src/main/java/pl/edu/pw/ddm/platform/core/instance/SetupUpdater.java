package pl.edu.pw.ddm.platform.core.instance;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class SetupUpdater {

    private final RestTemplate restTemplate;

    boolean updateSetup(InstanceConfig.InstanceNode node) {
        if (node.getLocalhostName() == null) {
            log.warn("No localhost name so not updating");
            return false;
        }

        var addr = InstanceConfigMapper.INSTANCE.map(node);
        var url = InstanceAgentAddressFactory.configSetup(addr);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>(1);
        body.add("masterPublicAddress", node.getLocalhostName());

        log.debug("Updating node setup for: '{}' with body: '{}'.", addr, body);
        var response = restTemplate.postForEntity(url, body, Void.class);
        log.info("Node setup updated with response code: '{}'", response.getStatusCodeValue());

        return response.getStatusCode() == HttpStatus.OK;
    }

}
