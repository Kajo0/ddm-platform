package pl.edu.pw.ddm.platform.core.algorithm;

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
class DefaultAlgorithmBroadcaster implements AlgorithmBroadcaster {

    private final AlgorithmLoader algorithmLoader;
    private final RestTemplate restTemplate;

    @Override
    public String broadcast(InstanceAddrDto masterAddr, AlgorithmLoader.AlgorithmDesc algorithmDesc) {
        log.info("Broadcasting algorithm with id '{}' and size '{}' into master node '{}'.", algorithmDesc.getId(),
                algorithmDesc.getSizeInBytes(), masterAddr);

        File algFile = algorithmLoader.load(algorithmDesc.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>(1);
        body.add("algorithmFile", new FileSystemResource(algFile));
        body.add("algorithmId", algorithmDesc.getId());
        body.add("algorithmPackageName", algorithmDesc.getPackageName());
        body.add("pipeline", algorithmDesc.getPipeline());

        String url = InstanceAgentAddressFactory.sendAlgorithm(masterAddr);
        ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        log.debug("Algorithm broadcast post response: '{}'.", response.getBody());

        return "ok_process-id";
    }

}
