package pl.edu.pw.ddm.platform.core.execution;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.io.PatternFilenameFilter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pl.edu.pw.ddm.platform.core.algorithm.AlgorithmFacade;
import pl.edu.pw.ddm.platform.core.data.DataFacade;
import pl.edu.pw.ddm.platform.core.instance.InstanceAgentAddressFactory;
import pl.edu.pw.ddm.platform.core.instance.InstanceFacade;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

@Slf4j
@Service
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class LocalResultsCollector implements ResultsCollector {

    // TODO move to properties
    private static final String RESULTS_PATH = "/coordinator/results";
    private static final String RESULTS_SUFFIX = "-results.txt";
    private static final String EXECUTION_DESCRIPTION = "description.properties";
    private static final String ARCHIVE_RESULTS_PATH = "/coordinator/archive_results";

    private static final FilenameFilter RESULT_FILE_FILTER = new PatternFilenameFilter(".*" + RESULTS_SUFFIX + "$");

    private final RestTemplate restTemplate;
    private final InstanceFacade instanceFacade;
    private final AlgorithmFacade algorithmFacade;
    private final DataFacade dataFacade;

    @SneakyThrows
    @Override
    public String collect(List<InstanceAddrDto> addresses, ExecutionStarter.ExecutionDesc desc) {
        // TODO enable after status check
//        if (ExecutionStarter.ExecutionDesc.ExecutionStatus.FINISHED != desc.getStatus()) {
//            log.warn("Execution '{}' not finished - in status '{}' so not collecting.", desc.getId(), desc.getStatus().getCode());
//            return desc.getStatus().getCode();
//        }

        // TODO check if already has results downloaded
        log.info("Collecting results using desc '{}' from nodes '{}'.", desc, addresses);
        List<InstanceAddrDto> workers = addresses.stream()
                .filter(InstanceAddrDto::isWorker)
                .collect(Collectors.toList());

        // TODO optimize
        List<NodeResultsPair> files = workers.stream()
                .map(addr -> requestForResults(addr, desc.getId()))
                .collect(Collectors.toList());
        log.info("Collected {} files with results from worker nodes.", files.size());
        saveResults(files, desc.getId());
        saveDescription(desc);

        return "ok";
    }

    // TODO move to loader or sth
    @Override
    public File[] load(String executionId) {
        Path path = Paths.get(RESULTS_PATH, executionId);
        Preconditions.checkState(path.toFile().exists(), "Result directory for execution id: '%s' not exists.", executionId);

        return path.toFile()
                .listFiles(RESULT_FILE_FILTER);
    }

    private NodeResultsPair requestForResults(InstanceAddrDto addressDto, String executionId) {
        restTemplate.getMessageConverters()
                .add(new ByteArrayHttpMessageConverter());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.TEXT_PLAIN));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = InstanceAgentAddressFactory.collectResults(addressDto, executionId);
        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
        if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new IllegalStateException("Results not found for execution id " + executionId + " and address: " + addressDto);
        }

        byte[] body = response.getBody();
        log.debug("Collect data get response length: '{}'.", body.length);

        return NodeResultsPair.of(addressDto.getId(), body);
    }

    private void saveResults(List<NodeResultsPair> files, String executionId) throws IOException {
        Path path = Paths.get(RESULTS_PATH, executionId);
        if (Files.exists(path)) {
            log.warn("Removing previous results path '{}'.", path);
        }
        FileUtils.deleteDirectory(path.toFile());
        Files.createDirectories(path);

        for (NodeResultsPair pair : files) {
            Path nodeFile = path.resolve(pair.nodeId + RESULTS_SUFFIX);
            log.info("Saving file '{}' with node results.", nodeFile);
            Files.write(nodeFile, pair.results);
        }
    }

    private void saveDescription(ExecutionStarter.ExecutionDesc desc) throws IOException {
        // TODO refactor a bit and save as .json
        var objMapper = new ObjectMapper();
        Properties prop = new Properties();
        prop.setProperty("workers", String.valueOf(instanceFacade.addresses(InstanceFacade.AddressRequest.of(desc.getInstanceId())).size() - 1));
        prop.setProperty("algorithm", objMapper.writeValueAsString(algorithmFacade.description(AlgorithmFacade.DescriptionRequest.of(desc.getAlgorithmId()))));
        prop.setProperty("trainData", objMapper.writeValueAsString(dataFacade.description(DataFacade.DescriptionRequest.of(desc.getTrainDataId()))));
        prop.setProperty("testData", objMapper.writeValueAsString(Optional.ofNullable(desc.getTestDataId()).map(id -> dataFacade.description(DataFacade.DescriptionRequest.of(id))).orElse(null)));
        prop.setProperty("execution", objMapper.writeValueAsString(desc));
        prop.setProperty("saveTimestamp", LocalDateTime.now().toString());

        Path path = Paths.get(RESULTS_PATH, desc.getId(), EXECUTION_DESCRIPTION);
        prop.store(Files.newOutputStream(path), null);
    }

    @PostConstruct
    void init() throws IOException {
        Files.createDirectories(Paths.get(RESULTS_PATH));
        Files.createDirectories(Paths.get(ARCHIVE_RESULTS_PATH));
    }

    @PreDestroy
    void destroy() throws IOException {
        log.info("PreDestroy " + this.getClass().getSimpleName());
        Path archive = Paths.get(ARCHIVE_RESULTS_PATH);
        File[] toMove = Paths.get(RESULTS_PATH)
                .toFile()
                .listFiles();

        log.info("{} results dir to archive.", toMove.length);
        for (File f : toMove) {
            log.info("Moving {} to archive.", f.getName());
            Files.move(f.toPath(), archive.resolve(f.getName()));
        }
    }

    @Value(staticConstructor = "of")
    private static class NodeResultsPair {

        private String nodeId;
        private byte[] results;
    }

}