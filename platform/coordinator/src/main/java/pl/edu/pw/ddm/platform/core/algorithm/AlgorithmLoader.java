package pl.edu.pw.ddm.platform.core.algorithm;

import java.io.File;
import java.util.Map;

import lombok.Value;

interface AlgorithmLoader {

    String save(String name, byte[] jar);

    File load(String algorithmId);

    AlgorithmDesc getAlgorithm(String algorithmId);

    Map<String, AlgorithmDesc> allAlgorithmsInfo();

    @Value
    class AlgorithmDesc {

        private String id;
        private String originalName;
        private String packageName;
        private String algorithmType;
        private String algorithmName;
        private Long sizeInBytes;
        private String location;
    }

}
