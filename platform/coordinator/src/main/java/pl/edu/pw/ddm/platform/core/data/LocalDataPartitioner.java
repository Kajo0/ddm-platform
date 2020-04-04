package pl.edu.pw.ddm.platform.core.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.SneakyThrows;
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

    @SneakyThrows
    @Override
    public String scatter(List<InstanceAddrDto> addresses, LocalDataLoader.DataDesc dataDesc, String strategy) {
        log.info("Scattering data '{}' with strategy '{}' into nodes '{}'.", dataDesc, strategy, addresses);
        // TODO eval strategy
        // TODO it may be path as well for FileSystemResource
        File dataFile = dataLoader.load(dataDesc.getId());
        List<InstanceAddrDto> workers = addresses.stream()
                .filter(addressDto -> "worker".equals(addressDto.getType()))
                .collect(Collectors.toList());
        List<Path> tempFiles = uniformDistribution(workers.size(), dataDesc, dataFile);

        // TODO optimize
        for (int i = 0; i < workers.size(); ++i) {
            InstanceAddrDto addressDto = workers.get(i);
            Path tempDataFile = tempFiles.get(i);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>(1);
            body.add("dataFile", new FileSystemResource(tempDataFile));
            body.add("dataId", dataDesc.getId());
            body.add("separator", dataDesc.getSeparator());
            body.add("idIndex", dataDesc.getIdIndex());
            body.add("labelIndex", dataDesc.getLabelIndex());
            body.add("attributesAmount", dataDesc.getAttributesAmount());
            body.add("colTypes", dataDesc.getColTypes());

            String url = InstanceAgentAddressFactory.sendData(addressDto);
            ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
            log.debug("Scattering data post response: '{}'.", response.getBody());
            Files.delete(tempDataFile);
        }

        return "ok_process-id";
    }

    private List<Path> uniformDistribution(int workers, LocalDataLoader.DataDesc dataDesc, File dataFile) throws IOException {
        List<Path> tempFiles = IntStream.range(0, workers)
                .mapToObj(wi -> {
                    try {
                        return Files.createTempFile("splitter.", String.valueOf(wi));
                    } catch (IOException e) {
                        // TODO optimize
                        e.printStackTrace();
                        return Path.of("/tmp/splitter. " + wi);
                    }
                })
                .collect(Collectors.toList());

        int partSize = (int) (dataDesc.getNumberOfSamples() / workers);
        int lastRest = (int) (dataDesc.getNumberOfSamples() % workers);
        List<Integer> shuffleIndices = new ArrayList<>(dataDesc.getNumberOfSamples().intValue());
        for (int i = 0; i < lastRest; ++i) {
            shuffleIndices.add(0);
        }
        for (int i = 0; i < workers; ++i) {
            for (int j = 0; j < partSize; ++j) {
                shuffleIndices.add(i);
            }
        }
        Collections.shuffle(shuffleIndices);

        AtomicInteger i = new AtomicInteger(0);
        Files.readAllLines(dataFile.toPath())
                .forEach(l -> {
                    try {
                        int index = i.getAndIncrement();
                        int fileNumber = shuffleIndices.get(index);
                        Path tempFile = tempFiles.get(fileNumber);
                        Files.writeString(tempFile, l + System.lineSeparator(), StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        // TODO optimize
                        e.printStackTrace();
                    }
                });

        return tempFiles;
    }

}
