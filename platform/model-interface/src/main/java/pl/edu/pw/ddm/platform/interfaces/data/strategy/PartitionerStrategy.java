package pl.edu.pw.ddm.platform.interfaces.data.strategy;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;

public interface PartitionerStrategy {

    String name();

    List<Path> partition(DataDesc dataDesc, StrategyParameters parameters, PartitionFileCreator partitionFileCreator) throws IOException;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    class StrategyParameters {

        private Long seed;
        private int partitions;
        private DistanceFunction distanceFunction;
        private String customParams;
    }

    @Data
    class DataDesc {

        private String id;
        private String originalName;
        private String type;
        private Long sizeInBytes;
        private Long numberOfSamples;
        private String separator;
        private Integer idIndex;
        private Integer labelIndex;
        private Integer attributesAmount;
        private String[] colTypes;
        private List<String> filesLocations;
        private List<Long> sizesInBytes;
        private List<Long> numbersOfSamples;
    }

}
