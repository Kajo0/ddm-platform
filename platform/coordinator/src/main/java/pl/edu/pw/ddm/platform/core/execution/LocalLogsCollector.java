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
import java.util.stream.Stream;

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

    @Override
    public String fetchSince(String executionId, String nodeId, Integer since) {
        return fetch(executionId, nodeId, since, null);
    }

    @Override
    public String fetchLast(String executionId, String nodeId, Integer last) {
        return fetch(executionId, nodeId, null, last);
    }

    @SneakyThrows
    public String fetch(String executionId, String nodeId, Integer start, Integer limitOrLast) {
        Path path = Paths.get(LOGS_PATH, executionId, nodeId + LOGFILE_SUFFIX);
        if (Files.notExists(path)) {
            log.warn("No log file '{}' exists.", path);
            return "no log";
        }

        Stream<String> lines = Files.lines(path);

        if (start != null && limitOrLast != null) {
            lines = lines.skip(start)
                    .limit(limitOrLast);
        } else if (start != null) {
            lines = lines.skip(start);
        } else if (limitOrLast != null) {
            // TODO optimize
            long count = Files.lines(path)
                    .count();
            lines = lines.skip(Math.max(0, count - limitOrLast));
        }

        return lines.collect(Collectors.joining(System.lineSeparator()));
    }

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
