package pl.edu.pw.ddm.platform.runner;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;
import pl.edu.pw.ddm.platform.runner.utils.AlgorithmProcessorInitializer;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class InitParamsDto implements Serializable {

    private String datasetsPath;
    private String trainDataId;
    private String testDataId;
    private String executionId;
    private String algorithmPackageName;
    private String distanceFunctionName;
    private String distanceFunctionPackageName;
    private Map<String, String> executionParams;

    DistanceFunction findDistanceFunction() {
        if (DistanceFunction.PredefinedNames.NONE.equals(distanceFunctionName)) {
            return null;
        } else {
            return AlgorithmProcessorInitializer.initDistanceFunction(distanceFunctionPackageName, distanceFunctionName);
        }
    }

}
