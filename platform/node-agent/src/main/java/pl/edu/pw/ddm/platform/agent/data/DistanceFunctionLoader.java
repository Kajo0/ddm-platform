package pl.edu.pw.ddm.platform.agent.data;

import java.nio.file.Path;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.edu.pw.ddm.platform.agent.data.dto.DistanceFunctionDesc;

public interface DistanceFunctionLoader {

    boolean save(byte[] bytes, DistanceFunctionDesc distanceFunctionDesc);

    Path pathToFile(String distanceFunctionId);

    DistanceFunctionDesc description(String distanceFunctionId);

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    enum DescriptionKey {
        ID("algorithmId"),
        PACKAGE("packageName"),
        FUNCTION_NAME("functionName");

        private String code;
    }

}
