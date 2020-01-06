package pl.edu.pw.ddm.platform.core.data;

import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class LocalDataLoader implements DataLoader {

    private final Map<String, DataDesc> dataMap = new HashMap<>();

    @Override
    public String load(@NonNull String uri, boolean deductType) {
        // TODO load data
        var id = String.valueOf(uri.hashCode());
        var location = new DataDesc.DataLocation(List.of("/dev/null"), List.of(0L), List.of(0L));
        var data = new DataDesc(id, "csv", (long) id.length(), 0L, 8, new String[0], location);

        if (deductType) {
            // TODO deduct type based on sample
            log.debug("Deducting type for data '{}'.", uri);
        }

        if (dataMap.putIfAbsent(id, data) != null) {
            log.warn("Loaded the same data as before with id '{}'.", id);
        }

        return id;
    }

    @Override
    public DataDesc getDataDesc(String datasetId) {
        return dataMap.get(datasetId);
    }

    @PreDestroy
    void destroy() {
        // TODO disable for persistent config
        log.info("PreDestroy " + this.getClass()
                .getSimpleName());
        dataMap.values()
                .stream()
                .map(DataDesc::getLocation)
                .map(DataDesc.DataLocation::getLocations)
                .flatMap(Collection::parallelStream)
                .forEach(location -> {
                    log.debug("Deleting data: '{}'.", location);
                });
    }

    @Value
    static class DataDesc {

        private String id;
        private String type;
        private Long size;
        private Long samples;
        private Integer columns;
        private String[] colType;
        private DataLocation location;

        @Value
        static class DataLocation {

            private List<String> locations;
            private List<Long> sizes;
            private List<Long> samples;

            boolean isPartitioned() {
                return locations.size() > 1;
            }
        }
    }

}
