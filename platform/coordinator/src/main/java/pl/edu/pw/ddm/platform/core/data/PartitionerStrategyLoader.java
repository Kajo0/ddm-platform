package pl.edu.pw.ddm.platform.core.data;

import java.io.File;
import java.util.Map;

import lombok.Value;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy;

interface PartitionerStrategyLoader {

    String save(MultipartFile file);

    File load(String strategyId);

    StrategyDesc getStrategyDesc(String strategyId);

    PartitionerStrategy getStrategyImpl(String strategyNameOrId);

    Map<String, StrategyDesc> allStrategiesInfo();

    @Value
    class StrategyDesc {

        private String id;
        private String originalName;
        private String packageName;
        private String strategyName;
        private Long sizeInBytes;
        private String location;
    }

}
