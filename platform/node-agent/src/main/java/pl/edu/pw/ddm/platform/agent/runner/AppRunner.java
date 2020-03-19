package pl.edu.pw.ddm.platform.agent.runner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.spark.launcher.SparkLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class AppRunner {

    private final RestTemplate restTemplate;
    private final String addressesApi;

    AppRunner(RestTemplate restTemplate, @Value("${coordinator.api.addresses}") String addressesApi) {
        this.restTemplate = restTemplate;
        this.addressesApi = addressesApi;
    }

    // FIXME change
    private static final String CENTRAL_RUNNER_JAR_PATH = "/home/mmarkiew/stud/ddm-platform/platform/app-runner-java/build/libs/app-runner-java-0.0.1-SNAPSHOT-all.jar";
//    private static final String CENTRAL_RUNNER_JAR_PATH = "/home/mmarkiew/stud/ddm-platform/platform/app-runner-scala/target/scala-2.11/app-runner-scala-assembly-0.1.jar";
    // TODO properties this
//    private static final String CENTRAL_RUNNER_JAR_PATH = "/spark-runner-0.0.1-SNAPSHOT.jar";
    private static final String CENTRAL_RUNNER_MAIN_CLASS = "pl.edu.pw.ddm.platform.runner.CentralRunner";
//    private static final String CENTRAL_RUNNER_MAIN_CLASS = "pl.edu.pw.ddm.platform.runner.ScalaCentralRunner";

    // TODO non static running apps map and block next one
    public String run(String instanceId, String algorithmId, String dataId) throws IOException {
        String executionId = UUID.randomUUID()
                .toString(); // TODO random;

        Pair<String, String> nodeAddresses = getNodesAddresses(instanceId);
        String masterNode = nodeAddresses.getLeft();
        String workerNodes = nodeAddresses.getRight();


        Process process = new SparkLauncher()
                .setSparkHome("/home/mmarkiew/dev/spark") // FIXME Spark home not found; set it explicitly or use the SPARK_HOME environment variable.
//                .setSparkHome() // Spark home not found; set it explicitly or use the SPARK_HOME environment variable.
//                .setSparkHome(System.getenv("SPARK_HOME"))
                .setMaster("spark://" + masterNode)
                .setAppName(algorithmId + " (" + dataId + ") " + executionId) // FIXME
                .setMainClass(CENTRAL_RUNNER_MAIN_CLASS)
//                .setDeployMode("cluster")
                .redirectToLog(log.getName())
                .addJar("/home/mmarkiew/Desktop/sample-algorithm-0.0.1-SNAPSHOT.jar") // TODO algorithmId jar
                .setAppResource(CENTRAL_RUNNER_JAR_PATH)
                .addAppArgs(masterNode, workerNodes, algorithmId, dataId) // TODO num of workers etc
                .addSparkArg("spark.locality.wait", "3600s")
                .launch();

        return "execution ID / " + process.toString();
    }

    /**
     * @param instanceId
     * @return comma separated master and workers addresses with ports eg. (sparkmaster:1234;worker1:1111,worker2:2222)
     */
    private Pair<String, String> getNodesAddresses(String instanceId) {
        InstanceAddrDto[] array = restTemplate.getForEntity(addressesApi + instanceId, InstanceAddrDto[].class)
                .getBody();
        Map<String, List<InstanceAddrDto>> map = Arrays.stream(array)
                .collect(Collectors.groupingBy(InstanceAddrDto::getType));

        String master = Optional.of(map.get("master"))
                .map(Iterables::getOnlyElement)
                .map(n -> n.address + ":" + n.port)
                .orElseThrow(IllegalArgumentException::new);
        String workers = map.get("worker")
                .stream()
                .map(n -> n.address + ":" + n.port)
                .collect(Collectors.joining(","));

        return Pair.of(master, workers);
    }

    @Data
    private static class InstanceAddrDto {
        private String type;
        private String address;
        private String port;
    }

}
