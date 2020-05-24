package pl.edu.pw.ddm.platform.agent.configuration;

import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class DockerConfigurationService implements ConfigurationService {

    @Value("${spark.master-host:}")
    private String sparkMasterHost;

    @Override
    public void setup(String masterPublicAddr) throws AccessDeniedException {
        log.info("Provided config: masterPublicAddress='{}'.", masterPublicAddr);
        try {
            if (StringUtils.isNotBlank(masterPublicAddr) && StringUtils.isNotEmpty(sparkMasterHost)) {
                updateMasterHost(masterPublicAddr);
            }
        } catch (AccessDeniedException e) {
            throw new AccessDeniedException("Cannot modify /etc/hosts file - check if app has permissions in docker container.");
        }
    }

    @SneakyThrows
    private boolean updateMasterHost(@NonNull String address) throws AccessDeniedException {
        Path hosts = Paths.get("/etc/hosts");
        boolean correct = Files.readAllLines(hosts)
                .stream()
                .filter(a -> a.endsWith(sparkMasterHost))
                .map(a -> StringUtils.substringBefore(a, sparkMasterHost))
                .map(String::trim)
                .anyMatch(address::equals);
        if (correct) {
            log.info("Master host '{}' has correct address '{}' set.", sparkMasterHost, address);
            return false;
        }

        List<String> lines = Files.readAllLines(hosts);
        String previous = lines.stream()
                .filter(a -> a.endsWith(sparkMasterHost))
                .map(a -> StringUtils.substringBefore(a, sparkMasterHost))
                .map(String::trim)
                .findFirst()
                .orElse(null);

        if (previous != null) {
            log.info("Updating master host '{}' with address '{}' (previously was: '{}').", sparkMasterHost, address, previous);
            lines = lines.stream()
                    .filter(a -> !a.endsWith(sparkMasterHost))
                    .collect(Collectors.toList());
        } else {
            log.info("Inserting master host '{}' with address '{}'.", sparkMasterHost, address);
        }

        lines.add(address + "\t" + sparkMasterHost);
        Files.write(hosts, lines);

        return true;
    }

}
