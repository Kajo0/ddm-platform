package pl.edu.pw.ddm.platform.runner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.lang3.NotImplementedException;
import pl.edu.pw.ddm.platform.interfaces.algorithm.DdmPipeline;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.CentralDdmPipeline;

@Data
class JsonArgsDto {

    private String masterNode;
    private String workerNodes;
    private String instanceId;
    private String algorithmId;
    private String algorithmPackageName;
    private String pipeline;
    private String executionPath;
    private String datasetsPath;
    private String trainDataId;
    private String testDataId;
    private String distanceFunctionId;
    private String distanceFunctionPackageName;
    private String distanceFunctionName;
    private String executionId;
    private Map<String, String> executionParams;

    @SneakyThrows
    DdmPipeline pipeline() {
        if (pipeline == null) {
            System.err.println("Old non-pipeline so creating it");
            // TODO FIXME compatibility but currently node-agent and AlgConfig is required so this is redundant check
            throw new NotImplementedException("Use new pipeline instead of old local-global model.");
        }
        return new ObjectMapper().readValue(pipeline, CentralDdmPipeline.class); // FIXME specify ddm type
    }

    List<String> getWorkerNodes() {
        return Arrays.asList(workerNodes.split(","));
    }

    static JsonArgsDto fromJson(String json) throws IOException {
        System.out.println("********************************");
        System.out.println("Reading args from json: " + json);
        System.out.println("********************************");
        return new ObjectMapper().readValue(json, JsonArgsDto.class);
    }

}
