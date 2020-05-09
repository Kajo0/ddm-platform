package pl.edu.pw.ddm.platform.core.execution;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pl.edu.pw.ddm.platform.core.instance.InstanceAgentAddressFactory;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

@Slf4j
@Service
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class LocalLogsCollector implements LogsCollector {

    // TODO move to properties
    private static final String LOGS_PATH = "/coordinator/logs";
    private static final String LOGFILE_SUFFIX = ".log";

    private final RestTemplate restTemplate;

    @SneakyThrows
    @Override
    public String collectAll(List<InstanceAddrDto> addresses, ExecutionStarter.ExecutionDesc desc) {
        // TODO check if already has results downloaded
        log.info("Collecting logs using desc '{}' from nodes '{}'.", desc, addresses);

        // TODO optimize
        List<NodeLogsPair> files = addresses.stream()
                .map(addr -> requestForLogs(addr, desc.getId(), desc.getAppId()))
                .collect(Collectors.toList());
        log.info("Collected {} files with logs from nodes.", files.size());

        saveLogs(files, desc.getId());
        return "ok";
    }

//    @Override
//    public File[] load(String executionId) {
//        Path path = Paths.get(RESULTS_PATH, executionId);
//        Preconditions.checkState(path.toFile().exists(), "Result directory for execution id: '%s' not exists.", executionId);
//
//        return path.toFile()
//                .listFiles(RESULT_FILE_FILTER);
//    }

    private NodeLogsPair requestForLogs(InstanceAddrDto addressDto, String executionId, String appId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.TEXT_PLAIN));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = InstanceAgentAddressFactory.collectLogs(addressDto, executionId, appId);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new IllegalStateException("Logs not found for execution id " + executionId + ", app id " + appId + " and address: " + addressDto);
        }

        String body = response.getBody();
        log.debug("Collect logs response length: '{}'.", body.length());

        return NodeLogsPair.of(addressDto.getId(), body);
    }

    private void saveLogs(List<NodeLogsPair> files, String executionId) throws IOException {
        Path path = Paths.get(LOGS_PATH, executionId);
        if (Files.exists(path)) {
            log.warn("Removing previous logs path '{}'.", path);
            FileUtils.deleteDirectory(path.toFile());
        }
        Files.createDirectories(path);

        for (NodeLogsPair pair : files) {
            Path nodeFile = path.resolve(pair.nodeId + LOGFILE_SUFFIX);
            log.info("Saving file '{}' with node logs.", nodeFile);
            Files.write(nodeFile, pair.logData.getBytes());
        }
    }

    @PostConstruct
    void init() throws IOException {
        Files.createDirectories(Paths.get(LOGS_PATH));
    }

    @PreDestroy
    void destroy() throws IOException {
        log.info("PreDestroy " + this.getClass().getSimpleName());

        Path path = Paths.get(LOGS_PATH);
        log.info("Removing logs directory with content.");
        FileUtils.deleteDirectory(path.toFile());
    }

    @Value(staticConstructor = "of")
    private static class NodeLogsPair {

        private String nodeId;
        private String logData;
    }

}
