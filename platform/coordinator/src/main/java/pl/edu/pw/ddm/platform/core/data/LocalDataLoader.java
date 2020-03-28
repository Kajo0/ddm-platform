package pl.edu.pw.ddm.platform.core.data;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
class LocalDataLoader implements DataLoader {

    private static final String DATA_PATH = "/tmp/coordinator/data";

    LocalDataLoader() throws IOException {
        // TODO save on PreDestroy and collect or keep removed
        Files.createDirectories(Paths.get(DATA_PATH));
    }

    private final Map<String, DataDesc> dataMap = new HashMap<>();

    @SneakyThrows
    @Override
    public String load(@NonNull String uri, boolean deductType) {
        // TODO load data
        var id = String.valueOf(uri.hashCode());
        String name = uri.substring(FilenameUtils.indexOfLastSeparator(uri) + 1);
        // TODO improve without copy
        File file = File.createTempFile(name, "tmp");
        FileUtils.copyURLToFile(new URL(uri), file);
        DataDesc data = saveAndPrepareDataDesc(id, FileUtils.readFileToByteArray(file), name, deductType);

        if (dataMap.put(id, data) != null) {
            log.warn("Loaded probably the same data as before with id '{}'.", id);
        }

        return id;
    }

    @SneakyThrows
    @Override
    public String load(MultipartFile file, boolean deductType) {
        var id = String.valueOf((System.currentTimeMillis() + file.getOriginalFilename()).hashCode());
        DataDesc data = saveAndPrepareDataDesc(id, file.getBytes(), file.getOriginalFilename(), deductType);

        if (dataMap.put(id, data) != null) {
            log.warn("Loaded probably the same data as before with id '{}'.", id);
        }

        return id;
    }

    @Override
    public DataDesc getDataDesc(String datasetId) {
        return dataMap.get(datasetId);
    }

    // TODO remove debug feature or change to proper immutable informational
    @Override
    public Map<String, DataDesc> allDataInfo() {
        return dataMap;
    }

    // TODO move to different component
    private DataDesc saveAndPrepareDataDesc(String id, byte[] bytes, String name, boolean deductType) throws IOException {
        // TODO check type inside file
        String type = FilenameUtils.getExtension(name);
        Path dataPath = Paths.get(DATA_PATH, id + "." + type);
        Files.write(dataPath, bytes);

        // TODO check if header and remove
        long lines = Files.lines(dataPath).count();
        long size = bytes.length;
        int columns = 0; // TODO count
        String[] types = new String[columns]; // TODO check

        var location = new DataDesc.DataLocation(List.of(dataPath.toString()), List.of(size), List.of(lines));

        if (deductType) {
            // TODO deduct type based on sample
//            types = ...;
            log.debug("Deducting type for data '{}'.", name);
        }

        return new DataDesc(id, name, type, size, lines, columns, types, location);
    }

    @PreDestroy
    void destroy() {
        // TODO disable for persistent config
        log.info("PreDestroy " + this.getClass().getSimpleName());
        dataMap.values()
                .stream()
                .map(DataDesc::getLocation)
                .map(DataDesc.DataLocation::getLocations)
                .flatMap(Collection::parallelStream)
                .forEach(location -> {
                    try {
                        log.debug("Deleting data: '{}'.", location);
                        Files.deleteIfExists(Path.of(location));
                    } catch (IOException e) {
                        log.error("Failed to remove file data: '{}'.", location);
                    }
                });
    }

    @Value
    static class DataDesc {

        private String id;
        private String originalName;
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
