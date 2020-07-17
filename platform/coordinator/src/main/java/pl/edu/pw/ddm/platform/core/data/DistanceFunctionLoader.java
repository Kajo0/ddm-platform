package pl.edu.pw.ddm.platform.core.data;

import java.io.File;
import java.util.Map;

import lombok.Value;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;

interface DistanceFunctionLoader {

    String save(MultipartFile file);

    File load(String distanceFunctionId);

    DistanceFunctionDesc getDistanceFunctionDesc(String distanceFunctionId);

    DistanceFunction getDistanceFunctionImpl(String distanceFunctionNameOrId);

    Map<String, DistanceFunctionDesc> allDistanceFunctionInfo();

    @Value
    class DistanceFunctionDesc {

        private String id;
        private String originalName;
        private String packageName;
        private String functionName;
        private Long sizeInBytes;
        private String location;
    }

}
