package pl.edu.pw.ddm.platform.core.instance;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class SetupUpdater {

    private final RestTemplate restTemplate;

    boolean updateSetup(InstanceConfig.InstanceData instance, InstanceConfig.InstanceNode node) {
        var addr = InstanceConfigMapper.INSTANCE.map(node);
        var url = InstanceAgentAddressFactory.configSetup(addr);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>(1);
        body.add("masterPublicAddress", prepareMasterPublicAddress(instance));

        log.debug("Updating node setup for: '{}' with body: '{}'.", addr, body);
        var response = restTemplate.postForEntity(url, body, Void.class);
        log.info("Node setup updated with response code: '{}'", response.getStatusCodeValue());

        return response.getStatusCode() == HttpStatus.OK;
    }

    private String prepareMasterPublicAddress(InstanceConfig.InstanceData instance) {
        var master = instance.getNodes()
                .values()
                .stream()
                .map(InstanceConfigMapper.INSTANCE::map)
                .filter(InstanceAddrDto::isMaster)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Master node not found in instance: " + instance.getId()));

        switch (instance.getType()) {
            case LOCAL_DOCKER:
                return master.getLocalhostIp();
            case MANUAL_SETUP:
                String addr = master.getAddress();
                if ("localhost".equals(addr)) {
                    return null;
                } else {
                    return addr;
                }

            default:
                throw new IllegalStateException("Unknown instance type: " + instance.getType());
        }
    }

}
