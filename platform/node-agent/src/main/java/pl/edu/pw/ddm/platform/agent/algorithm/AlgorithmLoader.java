package pl.edu.pw.ddm.platform.agent.algorithm;

import java.nio.file.Path;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.edu.pw.ddm.platform.agent.algorithm.dto.AlgorithmDesc;

public interface AlgorithmLoader {

    boolean save(byte[] bytes, AlgorithmDesc algorithmDesc);

    Path pathToFile(String algorithmId);

    AlgorithmDesc description(String algorithmId);

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    enum DescriptionKey {
        ID("algorithmId"),
        PACKAGE("packageName");

        private String code;
    }

}
