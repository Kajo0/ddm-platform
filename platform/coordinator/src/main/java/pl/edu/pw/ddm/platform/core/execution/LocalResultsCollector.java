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
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pl.edu.pw.ddm.platform.core.algorithm.AlgorithmFacade;
import pl.edu.pw.ddm.platform.core.data.DataFacade;
import pl.edu.pw.ddm.platform.core.instance.InstanceAgentAddressFactory;
import pl.edu.pw.ddm.platform.core.instance.InstanceFacade;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class LocalResultsCollector implements ResultsCollector {

    @Value("${paths.results.path}")
    private String resultsPath;

    @Value("${paths.results.archive-path}")
    private String archivePath;

    @Value("${paths.results.node-filename-suffix}")
    private String resultsSuffix;

    @Value("${paths.results.stats-filename}")
    private String statsFilename;

    @Value("${paths.results.execution-desc-filename}")
    private String executionDescFilename;

    private FilenameFilter resultFileFilter;

    private final RestTemplate restTemplate;
    private final InstanceFacade instanceFacade;
    private final AlgorithmFacade algorithmFacade;
    private final DataFacade dataFacade;

    @SneakyThrows
    @Override
    public String collect(List<InstanceAddrDto> addresses, ExecutionStarter.ExecutionDesc desc) {
        if (!desc.isCompleted()) {
            log.warn("Execution '{}' not completed - in status '{}' so not collecting.", desc.getId(), desc.getStatus().getCode());
            return desc.getStatus().getCode();
        }

        // TODO check if already has results downloaded
        log.info("Collecting results using desc '{}' from nodes '{}'.", desc, addresses);
        List<InstanceAddrDto> workers = addresses.stream()
                .filter(InstanceAddrDto::isWorker)
                .collect(Collectors.toList());
        InstanceAddrDto master = addresses.stream()
                .filter(InstanceAddrDto::isMaster)
                .findFirst()
                .orElseThrow();

        ExecutionStats stats = requestForStats(master, desc.getId());
        log.info("Collected results stats from master node.");

        // TODO optimize
        List<NodeResultsPair> files = workers.stream()
                .map(addr -> requestForResults(addr, desc.getId()))
                .collect(Collectors.toList());
        log.info("Collected {} files with results from worker nodes.", files.size());

        saveResults(files, desc.getId());
        saveStats(stats, desc.getId());
        saveDescription(desc);

        return "ok";
    }

    // TODO move to loader or sth
    @Override
    public File[] load(String executionId) {
        Path path = Paths.get(resultsPath, executionId);
        Preconditions.checkState(path.toFile().exists(), "Result directory for execution id: '%s' not exists.", executionId);

        return path.toFile()
                .listFiles(resultFileFilter);
    }

    // TODO move to loader or sth
    @SneakyThrows
    @Override
    public ExecutionStats loadStats(String executionId) {
        Path path = Paths.get(resultsPath, executionId, statsFilename);
        Preconditions.checkState(path.toFile().exists(), "Result stats file for execution id: '%s' not exists.", executionId);

        return new ObjectMapper().readValue(path.toFile(), ExecutionStats.class);
    }

    private NodeResultsPair requestForResults(InstanceAddrDto addressDto, String executionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.TEXT_PLAIN));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = InstanceAgentAddressFactory.collectResults(addressDto, executionId);
        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
        if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new IllegalStateException("Results not found for execution id " + executionId + " and address: " + addressDto);
        }

        byte[] body = response.getBody();
        if (body == null) {
            body = new byte[0];
        }
        log.debug("Collect results response length: '{}'.", body.length);

        return NodeResultsPair.of(addressDto.getId(), body);
    }

    private ExecutionStats requestForStats(InstanceAddrDto master, String executionId) {
        String url = InstanceAgentAddressFactory.collectResultsStats(master, executionId);
        ResponseEntity<ExecutionStats> response = restTemplate.getForEntity(url, ExecutionStats.class);
        if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new IllegalStateException("Results stats not found for execution id " + executionId + " and address: " + master);
        }

        ExecutionStats body = response.getBody();
        log.debug("Collected results stats response: '{}'.", body);
        return body;
    }

    private void saveResults(List<NodeResultsPair> files, String executionId) throws IOException {
        Path path = Paths.get(resultsPath, executionId);
        if (Files.exists(path)) {
            log.warn("Removing previous results path '{}'.", path);
            FileUtils.deleteDirectory(path.toFile());
        }
        Files.createDirectories(path);

        for (NodeResultsPair pair : files) {
            Path nodeFile = path.resolve(pair.nodeId + resultsSuffix);
            log.info("Saving file '{}' with node results.", nodeFile);
            Files.write(nodeFile, pair.results);
        }
    }

    private void saveStats(ExecutionStats stats, String executionId) throws IOException {
        Path path = Paths.get(resultsPath, executionId, statsFilename);
        log.info("Saving file '{}' with execution results stats.", path);
        String json = new ObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValueAsString(stats);
        Files.write(path, json.getBytes());
    }

    private void saveDescription(ExecutionStarter.ExecutionDesc desc) throws IOException {
        // TODO refactor a bit and save as .json
        // TODO save cpu & memory info -> instance config - EDIT: already present with workers and master cpu&memory info in description
        var objMapper = new ObjectMapper();
        Properties prop = new Properties();
        prop.setProperty("workers", String.valueOf(instanceFacade.addresses(InstanceFacade.AddressRequest.of(desc.getInstanceId())).size() - 1));
        prop.setProperty("algorithm", objMapper.writeValueAsString(algorithmFacade.description(AlgorithmFacade.DescriptionRequest.of(desc.getAlgorithmId()))));
        prop.setProperty("trainData", objMapper.writeValueAsString(dataFacade.description(DataFacade.DescriptionRequest.of(desc.getTrainDataId()))));
        prop.setProperty("testData", objMapper.writeValueAsString(Optional.ofNullable(desc.getTestDataId()).map(id -> dataFacade.description(DataFacade.DescriptionRequest.of(id))).orElse(null)));
        prop.setProperty("execution", objMapper.writeValueAsString(desc));
        prop.setProperty("trainDataScatterInfo", objMapper.writeValueAsString(instanceFacade.dataScatter(InstanceFacade.DataScatterRequest.of(desc.getInstanceId(), desc.getTrainDataId()))));
        prop.setProperty("saveTimestamp", LocalDateTime.now().toString());
        if (desc.getTestDataId() != null) {
            prop.setProperty("testDataScatterInfo", objMapper.writeValueAsString(instanceFacade.dataScatter(InstanceFacade.DataScatterRequest.of(desc.getInstanceId(), desc.getTestDataId()))));
        }

        Path path = Paths.get(resultsPath, desc.getId(), executionDescFilename);
        prop.store(Files.newOutputStream(path), null);
    }

    @PostConstruct
    void init() throws IOException {
        this.resultFileFilter = new PatternFilenameFilter(".*" + resultsSuffix + "$");

        Files.createDirectories(Paths.get(resultsPath));
        Files.createDirectories(Paths.get(archivePath));
    }

    @PreDestroy
    void destroy() throws IOException {
        log.info("PreDestroy " + this.getClass().getSimpleName());
        Path archive = Paths.get(archivePath);
        File[] toMove = Paths.get(resultsPath)
                .toFile()
                .listFiles();

        log.info("{} results dir to archive.", toMove.length);
        for (File f : toMove) {
            log.info("Moving {} to archive.", f.getName());
            Files.move(f.toPath(), archive.resolve(f.getName()));
        }
    }

    @lombok.Value(staticConstructor = "of")
    private static class NodeResultsPair {

        private String nodeId;
        private byte[] results;
    }

}
