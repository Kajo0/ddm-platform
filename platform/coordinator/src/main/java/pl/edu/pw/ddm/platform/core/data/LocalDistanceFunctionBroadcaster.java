package pl.edu.pw.ddm.platform.core.data;

import java.io.File;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import pl.edu.pw.ddm.platform.core.instance.InstanceAgentAddressFactory;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

@Slf4j
@Service
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class LocalDistanceFunctionBroadcaster implements DistanceFunctionBroadcaster {
    // TODO map of current scattered distance functions

    private final DistanceFunctionLoader distanceFunctionLoader;
    private final RestTemplate restTemplate;

    @Override
    public String broadcast(InstanceAddrDto masterAddr, DistanceFunctionLoader.DistanceFunctionDesc funcDesc) {
        log.info("Broadcasting distance function with id '{}', name '{}' and size '{}' into master node '{}'.", funcDesc.getId(), funcDesc.getFunctionName(), funcDesc.getSizeInBytes(), masterAddr);

        File funcFile = distanceFunctionLoader.load(funcDesc.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>(3);
        body.add("distanceFunctionFile", new FileSystemResource(funcFile));
        body.add("distanceFunctionId", funcDesc.getId());
        body.add("distanceFunctionPackage", funcDesc.getPackageName());
        body.add("distanceFunctionName", funcDesc.getFunctionName());

        String url = InstanceAgentAddressFactory.sendDistanceFunction(masterAddr);
        ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        log.debug("Distance function broadcast post response: '{}'.", response.getBody());

        return "ok_process-id";
    }

}
