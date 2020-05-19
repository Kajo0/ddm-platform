package pl.edu.pw.ddm.platform.agent.runner;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.apache.spark.launcher.SparkAppHandle;
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

    @Value("${paths.datasets}")
    private String datasetsPath;

    @Value("${paths.execution.path}")
    private String executionPath;

    @Value("${paths.execution.logs.central-filename}")
    private String centralLogFilename;

    @Value("${spark.master-port}")
    private String sparkMasterPort;

    private final RestTemplate restTemplate;
    private final AlgorithmLoader algorithmLoader;
    private final DistanceFunctionLoader distanceFunctionLoader;
    private final LocalExecutionStatusPersister executionStatusPersister;
    private final CoordinatorApiConfig coordinatorApiConfig;
    private final String sparkHome;
    private final String runnerJarPath;
    private final String runnerMainClass;
    private final List<String> algorithmsJarsPaths;
    private final boolean agentOnMasterNode;

    private String coordinatorBaseUrl;

    private SparkAppHandle runningApp;

    AppRunner(RestTemplate restTemplate,
              AlgorithmLoader algorithmLoader,
              DistanceFunctionLoader distanceFunctionLoader,
              LocalExecutionStatusPersister executionStatusPersister,
              AppAlgorithmsConfig algorithmsConfig,
              CoordinatorApiConfig coordinatorApiConfig,
              Environment env,
              @Value("${spark.home}") String sparkHome) {
        this.restTemplate = restTemplate;
        this.algorithmLoader = algorithmLoader;
        this.distanceFunctionLoader = distanceFunctionLoader;
        this.executionStatusPersister = executionStatusPersister;
        this.sparkHome = sparkHome;
        this.coordinatorApiConfig = coordinatorApiConfig;
        this.runnerJarPath = algorithmsConfig.getRunner().getPath();
        this.runnerMainClass = algorithmsConfig.getRunner().getMainClass();
        this.algorithmsJarsPaths = algorithmsConfig.getAlgorithms();
        this.agentOnMasterNode = env.acceptsProfiles(Profiles.of(ProfileConstants.NON_DEVELOPMENT_MASTER_NODE));

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
                .redirectOutput(prepareMasterLogFile(executionId))
                .addSparkArg("spark.locality.wait", "3600s")
                .setDeployMode("client")
                .setConf("spark.task.maxFailures", "1")
                .setConf(SparkLauncher.EXECUTOR_CORES, String.valueOf(params.cpuCount))
                .setConf(SparkLauncher.EXECUTOR_MEMORY, params.memoryInGb + "g");

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

        String jsonArgs = jsonArgsBuilder.datasetsPath(datasetsPath)
                .executionPath(executionPath)
                .build()
                .toJsonArgs();
        log.info("Launching spark process on master '{}' with args: '{}'.", masterNode, jsonArgs);
        runningApp = launcher.addAppArgs(jsonArgs)
                .startApplication(new AppHandleListener());

        return executionId;
    }

    public boolean isProgramRunning() {
        return runningApp != null && !runningApp.getState().isFinal();
    }

    public boolean stopProgram(String executionId, String appId) {
        // TODO make use of arguments
        if (!isProgramRunning()) {
            log.warn("No App is not running.");
            return false;
        } else {
            // TODO check for events if really stopped and eventually kill it
            log.warn("Stopping program with executionId '{}' and appId '{}'.", executionId, appId);
            runningApp.stop();
            try {
                executionStatusPersister.stop(executionId);
            } catch (IOException e) {
                log.warn("Status not found so creating stopped one for executionId '{}' and appId '{}'.", executionId, appId);
                executionStatusPersister.init(executionId, appId);
                try {
                    executionStatusPersister.stop(executionId);
                } catch (IOException ex) {
                    log.error("Exception during saving stop status for app.", ex);
                    // ignore
                }
            }
            return true;
        }
    }

    private File prepareMasterLogFile(String executionId) throws IOException {
        Path path = Paths.get(executionPath, executionId, centralLogFilename);
        Files.createDirectories(path.getParent());
        return Files.createFile(path)
                .toFile();
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
            master = InetAddress.getLocalHost().getHostName() + ":" + sparkMasterPort;
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
                    .exec("traceroute -m 1 8.8.8.8");
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

    // TODO remove execution artifacts on destroy? when no docker

    @Builder
    public static class AppRunnerParamsDto {

        private String instanceId;
        private String algorithmId;
        private String trainDataId;
        private String testDataId;
        private String distanceFunctionId;
        private String distanceFunctionName;
        private Map<String, String> executionParams;

        private Integer cpuCount;
        private Integer memoryInGb;
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
        private String executionPath;
        private String datasetsPath;
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
