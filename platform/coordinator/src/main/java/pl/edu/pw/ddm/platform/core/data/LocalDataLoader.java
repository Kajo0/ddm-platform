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
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pw.ddm.platform.core.util.IdGenerator;

@Slf4j
@Service
class LocalDataLoader implements DataLoader {

    // TODO move to properties
    private static final String DATA_PATH = "/coordinator/data";

    private final Map<String, DataDesc> dataMap = new HashMap<>();

    LocalDataLoader() throws IOException {
        // TODO save on PreDestroy and collect or keep removed
        Files.createDirectories(Paths.get(DATA_PATH));
    }

    @SneakyThrows
    @Override
    public String save(@NonNull String uri, String separator, Integer idIndex, Integer labelIndex, boolean deductType) {
        var id = IdGenerator.generate(uri);
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
        var id = IdGenerator.generate(file.getOriginalFilename() + file.getSize());
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

        return new DataDescriber(dataPath, id, name, separator, idIndex, labelIndex, deductType)
                .describe();
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

}
