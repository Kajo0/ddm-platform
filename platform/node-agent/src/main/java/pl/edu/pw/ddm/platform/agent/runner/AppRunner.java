package pl.edu.pw.ddm.platform.agent.runner;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Iterables;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.spark.launcher.SparkLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pl.edu.pw.ddm.platform.agent.algorithm.AlgorithmLoader;

@Slf4j
@Service
@EnableConfigurationProperties({AppAlgorithmsConfig.class, CoordinatorApiConfig.class})
public class AppRunner {

    private static final String SPARK_MASTER_PORT = "7077";

    private final RestTemplate restTemplate;
    private final AlgorithmLoader algorithmLoader;
    private final CoordinatorApiConfig coordinatorApiConfig;
    private final String sparkHome;
    private final String runnerJarPath;
    private final String runnerMainClass;
    private final List<String> algorithmsJarsPaths;
    private final boolean agentOnMasterNode;

    private String coordinatorBaseUrl;

    AppRunner(RestTemplate restTemplate,
              AlgorithmLoader algorithmLoader,
              AppAlgorithmsConfig algorithmsConfig,
              CoordinatorApiConfig coordinatorApiConfig,
              Environment env,
              @Value("${spark.home}") String sparkHome) {
        this.restTemplate = restTemplate;
        this.algorithmLoader = algorithmLoader;
        this.sparkHome = sparkHome;
        this.coordinatorApiConfig = coordinatorApiConfig;
        this.runnerJarPath = algorithmsConfig.getRunner().getPath();
        this.runnerMainClass = algorithmsConfig.getRunner().getMainClass();
        this.algorithmsJarsPaths = algorithmsConfig.getAlgorithms();
        this.agentOnMasterNode = env.acceptsProfiles(Profiles.of("nodemaster"));

        this.coordinatorBaseUrl = coordinatorApiConfig.getBaseUrl();
    }

    // TODO running apps map and block next one
    public String run(String instanceId, String algorithmId, String dataId) throws IOException {
        String executionId = UUID.randomUUID()
                .toString(); // TODO random;

        Pair<String, String> nodeAddresses = getNodesAddresses(instanceId);
        String masterNode = nodeAddresses.getLeft();
        String workerNodes = nodeAddresses.getRight();

        SparkLauncher launcher = new SparkLauncher()
                .setSparkHome(sparkHome)
                .setMaster("spark://" + masterNode)
                .setAppName(algorithmId + " (" + dataId + ") " + executionId) // FIXME
                .setAppResource(runnerJarPath)
                .setMainClass(runnerMainClass)
                .redirectToLog(log.getName())
                .addAppArgs(masterNode, workerNodes, algorithmId, dataId)
                .addSparkArg("spark.locality.wait", "3600s");

        File algFile = algorithmLoader.load(algorithmId);
        launcher.addJar(algFile.toString());
        // TODO optionally add predefined if requested
//        for (String jar : algorithmsJarsPaths) {
//            launcher.addJar(jar);
//        }

        log.info("Launching spark process on master '{}' for algorithm '{}' and data '{}'.", masterNode, algorithmId, dataId);
        Process process = launcher.launch();
        return "execution ID / " + process.toString();
    }

    /**
     * @param instanceId
     * @return comma separated master and workers addresses with ports eg. (sparkmaster:1234;worker1:1111,worker2:2222)
     */
    @SneakyThrows
    private Pair<String, String> getNodesAddresses(String instanceId) {
        String url = coordinatorBaseUrl + coordinatorApiConfig.getAddresses() + instanceId;
        InstanceAddrDto[] array = restTemplate.getForEntity(url, InstanceAddrDto[].class)
                .getBody();
        Map<String, List<InstanceAddrDto>> map = Arrays.stream(array)
                .collect(Collectors.groupingBy(InstanceAddrDto::getType));

        String master;
        if (agentOnMasterNode) {
            master = InetAddress.getLocalHost().getHostName() + ":" + SPARK_MASTER_PORT;
            log.info("Setting spark master address to: '{}'.", master);
        } else {
            master = Optional.of(map.get("master"))
                    .map(Iterables::getOnlyElement)
                    .map(n -> n.address + ":" + n.port)
                    .orElseThrow(IllegalArgumentException::new);
        }

        // TODO check those worker address are correct from coordinator point of view not from docker
        String workers = map.get("worker")
                .stream()
                .map(n -> n.address + ":" + n.port)
                .collect(Collectors.joining(","));

        return Pair.of(master, workers);
    }

    @PostConstruct
    private void findGateway() throws IOException {
        if (StringUtils.isBlank(coordinatorApiConfig.getHost())) {
            Process result = Runtime.getRuntime()
                    .exec("traceroute -m 1 www.google.com");
            try (InputStreamReader isr = new InputStreamReader(result.getInputStream()); BufferedReader output = new BufferedReader(isr)) {
                output.readLine(); // skip line
                StringTokenizer tokenizer = new StringTokenizer(output.readLine());
                tokenizer.nextToken(); // skip token
                // TODO also https?
                this.coordinatorBaseUrl = Stream.of("http://" + tokenizer.nextToken(), coordinatorApiConfig.getPort())
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(":"));
                log.info("Setting coordinator base URL from gateway with provided port as: '{}'.", this.coordinatorBaseUrl);
            } catch (IOException e) {
                throw e;
            }
        }
    }

    @Data
    private static class InstanceAddrDto {
        private String type;
        private String address;
        private String port;
    }

}
