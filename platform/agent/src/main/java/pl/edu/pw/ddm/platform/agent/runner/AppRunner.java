package pl.edu.pw.ddm.platform.agent.runner;

import java.io.IOException;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.launcher.SparkLauncher;

@Slf4j
public class AppRunner {

    // FIXME change
    private static final String CENTRAL_RUNNER_JAR_PATH = "/home/mmarkiew/stud/ddm-platform/platform/spark-runner/build/libs/spark-runner-0.0.1-SNAPSHOT.jar";
    private static final String CENTRAL_RUNNER_MAIN_CLASS = "pl.edu.pw.ddm.platform.runner.CentralRunner";

    // TODO non static running apps map and block next one
    public static String run(String algorithmId, String dataId) throws IOException {
        String executionId = UUID.randomUUID()
                .toString(); // TODO random;

        Process process = new SparkLauncher()
                .setSparkHome("/home/mmarkiew/dev/spark") // FIXME Spark home not found; set it explicitly or use the SPARK_HOME environment variable.
//                .setSparkHome() // Spark home not found; set it explicitly or use the SPARK_HOME environment variable.
//                .setSparkHome(System.getenv("SPARK_HOME"))
                .setMaster("spark://localhost:" + dataId) // FIXME obtain address
                .setAppName(algorithmId + " (" + dataId + ") " + executionId) // FIXME
                .setMainClass(CENTRAL_RUNNER_MAIN_CLASS)
                .redirectToLog(log.getName())
//                .addJar("/path/to/Jar") // TODO algorithmId jar
                .setAppResource(CENTRAL_RUNNER_JAR_PATH)
                .addAppArgs(algorithmId, dataId) // TODO num of workers etc
                .launch();

        return "execution ID / " + process.pid();
    }

}
