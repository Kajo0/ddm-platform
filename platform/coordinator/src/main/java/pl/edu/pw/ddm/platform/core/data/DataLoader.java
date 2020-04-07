package pl.edu.pw.ddm.platform.core.data;

import java.io.File;
import java.util.List;
import java.util.Map;

import lombok.Value;
import org.springframework.web.multipart.MultipartFile;

interface DataLoader {

    String save(String uri, String separator, Integer idIndex, Integer labelIndex, boolean deductType);

    String save(MultipartFile file, String separator, Integer idIndex, Integer labelIndex, boolean deductType);

    File load(String dataId);

    DataDesc getDataDesc(String datasetId);

    Map<String, DataDesc> allDataInfo();

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

        int dataAttributes() {
            int minus = 0;
            if (idIndex != null) {
                ++minus;
            }
            if (labelIndex != null) {
                ++minus;
            }
            return attributesAmount - minus;
        }

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

}
