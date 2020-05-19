package pl.edu.pw.ddm.platform.runner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

@Data
class JsonArgsDto {

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
