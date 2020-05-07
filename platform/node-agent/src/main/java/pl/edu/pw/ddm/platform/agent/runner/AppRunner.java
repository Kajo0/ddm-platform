package pl.edu.pw.ddm.platform.agent.runner;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
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
import pl.edu.pw.ddm.platform.agent.data.DistanceFunctionLoader;
import pl.edu.pw.ddm.platform.agent.util.IdGenerator;
import pl.edu.pw.ddm.platform.agent.util.ProfileConstants;
import pl.edu.pw.ddm.platform.distfunc.EuclideanDistance;

@Slf4j
@Service
@EnableConfigurationProperties({AppAlgorithmsConfig.class, CoordinatorApiConfig.class})
public class AppRunner {

    private static final String SPARK_MASTER_PORT = "7077";

    private final RestTemplate restTemplate;
    private final AlgorithmLoader algorithmLoader;
    private final DistanceFunctionLoader distanceFunctionLoader;
    private final CoordinatorApiConfig coordinatorApiConfig;
    private final String sparkHome;
    private final String runnerJarPath;
    private final String runnerMainClass;
    private final List<String> algorithmsJarsPaths;
    private final boolean agentOnMasterNode;

    private String coordinatorBaseUrl;

    private Process runningApp;

    AppRunner(RestTemplate restTemplate,
              AlgorithmLoader algorithmLoader,
              DistanceFunctionLoader distanceFunctionLoader,
              AppAlgorithmsConfig algorithmsConfig,
              CoordinatorApiConfig coordinatorApiConfig,
              Environment env,
              @Value("${spark.home}") String sparkHome) {
        this.restTemplate = restTemplate;
        this.algorithmLoader = algorithmLoader;
        this.distanceFunctionLoader = distanceFunctionLoader;
        this.sparkHome = sparkHome;
        this.coordinatorApiConfig = coordinatorApiConfig;
        this.runnerJarPath = algorithmsConfig.getRunner().getPath();
        this.runnerMainClass = algorithmsConfig.getRunner().getMainClass();
        this.algorithmsJarsPaths = algorithmsConfig.getAlgorithms();
        this.agentOnMasterNode = env.acceptsProfiles(Profiles.of(ProfileConstants.NODE_MASTER));

        this.coordinatorBaseUrl = coordinatorApiConfig.getBaseUrl();
    }

    public String run(AppRunnerParamsDto params) throws IOException {
        if (isProgramRunning()) {
            throw new IllegalStateException("App already running");
        }

        String executionId = IdGenerator.generate();

        Pair<String, String> nodeAddresses = getNodesAddresses(params.instanceId);
        String masterNode = nodeAddresses.getLeft();
        String workerNodes = nodeAddresses.getRight();

        SparkLauncher launcher = new SparkLauncher()
                .setSparkHome(sparkHome)
                .setMaster("spark://" + masterNode)
                .setAppName(params.algorithmId + " (" + params.trainDataId + ") " + executionId) // FIXME
                .setAppResource(runnerJarPath)
                .setMainClass(runnerMainClass)
                .redirectToLog(log.getName())
                .addSparkArg("spark.locality.wait", "3600s")
                .setDeployMode("client")
                .setConf(SparkLauncher.EXECUTOR_MEMORY, "4g"); // TODO change it based on params and instance availability

        ArgJsonBuilder.ArgJsonBuilderBuilder jsonArgsBuilder = ArgJsonBuilder.toBuilder(params, masterNode, workerNodes, executionId);

        Path algPath = algorithmLoader.pathToFile(params.algorithmId);
        launcher.addJar(algPath.toString());
        // TODO optionally add predefined if requested

        jsonArgsBuilder.algorithmPackageName(algorithmLoader.description(params.algorithmId).getPackageName());

        if (params.distanceFunctionId != null) {
            log.info("Adding jar with custom distance function with id '{}' and name '{}'.", params.distanceFunctionId, params.distanceFunctionName);
            Path funcPath = distanceFunctionLoader.pathToFile(params.distanceFunctionId);
            launcher.addJar(funcPath.toString());

            jsonArgsBuilder.distanceFunctionPackageName(distanceFunctionLoader.description(params.distanceFunctionId).getPackageName());
        } else {
            log.info("Using predefined distance function: '{}'.", params.distanceFunctionName);
            jsonArgsBuilder.distanceFunctionPackageName(EuclideanDistance.class.getPackage().getName());
        }

        String jsonArgs = jsonArgsBuilder.build()
                .toJsonArgs();
        log.info("Launching spark process on master '{}' with args: '{}'.", masterNode, jsonArgs);
        runningApp = launcher.addAppArgs(jsonArgs)
                .launch();

        return executionId;
    }

    public boolean isProgramRunning() {
        return runningApp != null && runningApp.isAlive();
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

    @Builder
    public static class AppRunnerParamsDto {

        private String instanceId;
        private String algorithmId;
        private String trainDataId;
        private String testDataId;
        private String distanceFunctionId;
        private String distanceFunctionName;
        private Map<String, String> executionParams;
    }

    @Data
    private static class InstanceAddrDto {
        private String type;
        private String address;
        private String port;
    }

    @Getter
    @Builder
    private static class ArgJsonBuilder {

        private String masterNode;
        private String workerNodes;
        private String instanceId;
        private String algorithmId;
        private String algorithmPackageName;
        private String trainDataId;
        private String testDataId;
        private String distanceFunctionId;
        private String distanceFunctionPackageName;
        private String distanceFunctionName;
        private String executionId;
        private Map<String, String> executionParams;

        private static ArgJsonBuilderBuilder toBuilder(AppRunnerParamsDto params, String masterNode, String workerNodes, String executionId) {
            return ArgJsonBuilder.builder()
                    .masterNode(masterNode)
                    .workerNodes(workerNodes)
                    .executionId(executionId)
                    .instanceId(params.instanceId)
                    .algorithmId(params.algorithmId)
                    .trainDataId(params.trainDataId)
                    .testDataId(params.testDataId)
                    .distanceFunctionId(params.distanceFunctionId)
                    .distanceFunctionName(params.distanceFunctionName)
                    .executionParams(params.executionParams);
        }

        private String toJsonArgs() throws JsonProcessingException {
            return new ObjectMapper().writeValueAsString(this);
        }
    }

}
