package pl.edu.pw.ddm.platform.runner;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;
import pl.edu.pw.ddm.platform.runner.utils.AlgorithmProcessorInitializer;

@Data
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class InitParamsDto implements Serializable {

    private final String dataId;
    private final String executionId;
    private final String algorithmPackageName;
    private final String distanceFunctionName;
    private final String distanceFunctionPackageName;

    DistanceFunction findDistanceFunction() {
        if (DistanceFunction.PredefinedNames.NONE.equals(distanceFunctionName)) {
            return null;
        } else {
            return AlgorithmProcessorInitializer.initDistanceFunction(distanceFunctionPackageName, distanceFunctionName);
        }
    }

}
