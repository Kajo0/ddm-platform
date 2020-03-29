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
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.NotImplementedException;
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
    public String save(@NonNull String uri, String separator, Integer idIndex, Integer labelIndex, boolean deductType) {
        // TODO load data
        var id = String.valueOf(uri.hashCode());
        String name = uri.substring(FilenameUtils.indexOfLastSeparator(uri) + 1);
        // TODO improve without copy
        File file = File.createTempFile(name, "tmp");
        FileUtils.copyURLToFile(new URL(uri), file);
        DataDesc data = saveAndPrepareDataDesc(id, FileUtils.readFileToByteArray(file), name, separator, idIndex, labelIndex, deductType);

        if (dataMap.put(id, data) != null) {
            log.warn("Loaded probably the same data as before with id '{}'.", id);
        }

        return id;
    }

    @SneakyThrows
    @Override
    public String save(MultipartFile file, String separator, Integer idIndex, Integer labelIndex, boolean deductType) {
        var id = String.valueOf((System.currentTimeMillis() + file.getOriginalFilename()).hashCode());
        DataDesc data = saveAndPrepareDataDesc(id, file.getBytes(), file.getOriginalFilename(), separator, idIndex, labelIndex, deductType);

        if (dataMap.put(id, data) != null) {
            log.warn("Loaded probably the same data as before with id '{}'.", id);
        }

        return id;
    }

    @SneakyThrows
    @Override
    public File load(String dataId) {
        DataDesc desc = getDataDesc(dataId);
        DataDesc.DataLocation location = desc.getLocation();
        if (location.isPartitioned()) {
            throw new NotImplementedException("Loading partitioned data not implemented yet.");
        }
        log.debug("Loading data with id '{}' from '{}'.", dataId, location.getFilesLocations());

        return Optional.of(location)
                .map(DataDesc.DataLocation::getFilesLocations)
                .map(Iterables::getOnlyElement)
                .map(Path::of)
                .map(Path::toFile)
                .get();
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
    private DataDesc saveAndPrepareDataDesc(String id, byte[] bytes, String name, String separator, Integer idIndex, Integer labelIndex, boolean deductType) throws IOException {
        // TODO check type inside file
        String type = FilenameUtils.getExtension(name);
        Path dataPath = Paths.get(DATA_PATH, id + "." + type);
        Files.write(dataPath, bytes);

        if (idIndex == null) {
            indexData(dataPath, separator);
            idIndex = 0;
            ++labelIndex;
        }

        // TODO check if header and remove
        // TODO optimize if added index
        long lines = Files.lines(dataPath)
                .filter(Predicate.not(String::isBlank))
                .count();
        long size = bytes.length;
        // TODO optimize
        int attributesCount = Files.lines(dataPath)
                .findFirst()
                .map(l -> l.split(separator))
                .map(c -> c.length)
                .orElseThrow(() -> new IllegalArgumentException("Cannot count attributes by splitting first line using separator: " + separator));
        String[] types = new String[attributesCount]; // TODO check types

        var location = new DataDesc.DataLocation(List.of(dataPath.toString()), List.of(size), List.of(lines));

        if (deductType) {
            // TODO deduct type based on sample
//            types = ...;
            log.debug("Deducting type for data '{}'.", name);
        }

        return new DataDesc(id, name, type, size, lines, separator, idIndex, labelIndex, attributesCount, types, location);
    }

    private void indexData(Path dataPath, String separator) throws IOException {
        int[] i = new int[]{0};
        String lines = Files.lines(dataPath)
                .filter(Predicate.not(String::isBlank))
                .map(l -> (i[0]++) + separator + l)
                .collect(Collectors.joining(System.lineSeparator()));
        Files.writeString(dataPath, lines);
    }

    @PreDestroy
    void destroy() {
        // TODO disable for persistent config
        log.info("PreDestroy " + this.getClass().getSimpleName());
        dataMap.values()
                .stream()
                .map(DataDesc::getLocation)
                .map(DataDesc.DataLocation::getFilesLocations)
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
