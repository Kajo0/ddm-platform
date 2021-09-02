package pl.edu.pw.ddm.platform.interfaces.data.strategy;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public interface PartitionerStrategy {

    String name();

    default Random prepareRandom(StrategyParameters strategyParameters) {
        if (strategyParameters.getSeed() != null) {
            return new Random(strategyParameters.getSeed());
        } else {
            return new Random();
        }
    }

    default List<CustomParamDesc> availableParameters() {
        return Collections.singletonList(CustomParamDesc.of("dummy", Void.class, "no desc provided"));
    }

    default String printAvailableParameters() {
        List<CustomParamDesc> params = availableParameters();
        if (params == null) {
            return "no params described";
        } else {
            StringBuilder str = new StringBuilder();
            for (CustomParamDesc param : params) {
                str.append(param.key);
                str.append(" [");
                str.append(param.type.getSimpleName());
                str.append("]: ");
                str.append(param.desc);
                Arrays.stream(param.options)
                        .forEach(o -> str.append(System.lineSeparator())
                                .append("  - ")
                                .append(o));
                str.append(System.lineSeparator());
            }
            return str.toString();
        }
    }

    List<Path> partition(DataDesc dataDesc,
                         StrategyParameters parameters,
                         PartitionFileCreator partitionFileCreator) throws IOException;

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

    @Value
    class CustomParamDesc {

        public static CustomParamDesc of(String key, Class<?> type, String desc, String... options) {
            return new CustomParamDesc(key, type, desc, options);
        }

        String key;
        Class<?> type;
        String desc;
        String[] options;
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
