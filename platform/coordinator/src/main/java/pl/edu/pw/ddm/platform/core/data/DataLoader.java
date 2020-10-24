package pl.edu.pw.ddm.platform.core.data;

import java.io.File;
import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.web.multipart.MultipartFile;

interface DataLoader {

    String save(String uri, String separator, Integer idIndex, Integer labelIndex, DataOptions dataOptions);

    List<String> saveExtractTrain(String uri, String separator, Integer idIndex, Integer labelIndex, DataOptions dataOptions);

    String save(MultipartFile file, String separator, Integer idIndex, Integer labelIndex, DataOptions dataOptions);

    List<String> saveExtractTrain(MultipartFile file, String separator, Integer idIndex, Integer labelIndex, DataOptions dataOptions);

    File load(String dataId);

    DataDesc getDataDesc(String datasetId);

    Map<String, DataDesc> allDataInfo();

    @Value
    class DataOptions {

        private boolean deductType;
        private boolean vectorizeStrings;
        private Integer extractTrainPercentage;
        private Long seed;
    }

    @Value
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
        private DataLocation location;

        @Value
        static class DataLocation {

            private List<String> filesLocations;
            private List<Long> sizesInBytes;
            private List<Long> numbersOfSamples;

            boolean isPartitioned() {
                return filesLocations.size() > 1;
            }
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    enum TypeCode {
        TRAIN("train"),
        TEST("test");

        private final String code;

        static TypeCode fromCode(String typeCode) {
            for (TypeCode tc : values()) {
                if (tc.code.equals(typeCode)) {
                    return tc;
                }
            }
            throw new IllegalArgumentException("TypeCode not found for code: " + typeCode);
        }
    }

}
