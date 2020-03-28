package pl.edu.pw.ddm.platform.core.data;

import java.io.File;
import java.util.List;

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
class LocalDataPartitioner implements DataPartitioner {
    // TODO map of current scattered data

    private final DataLoader dataLoader;
    private final RestTemplate restTemplate;

    LocalDataPartitioner(DataLoader dataLoader, RestTemplate restTemplate) {
        this.dataLoader = dataLoader;
        this.restTemplate = restTemplate;
    }

    @Override
    public String scatter(List<InstanceAddrDto> addresses, LocalDataLoader.DataDesc dataDesc, String strategy) {
        log.info("Scattering data '{}' with strategy '{}' into nodes '{}'.", dataDesc, strategy, addresses);
        // TODO eval strategy
        // TODO it may be path as well for FileSystemResource
        File data = dataLoader.load(dataDesc.getId());

        // TODO optimize
        addresses.stream()
                .filter(addressDto -> "worker".equals(addressDto.getType()))
                .forEach(addressDto -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

                    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>(1);
                    body.add("dataFile", new FileSystemResource(data));

                    String url = InstanceAgentAddressFactory.sendData(addressDto, dataDesc.getId());
                    ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
                    log.debug("Scattering data post response: '{}'.", response.getBody());
                });

        return "ok_process-id";
    }

}
