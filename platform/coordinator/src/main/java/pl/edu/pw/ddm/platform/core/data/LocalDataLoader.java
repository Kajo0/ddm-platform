package pl.edu.pw.ddm.platform.core.data;

import javax.annotation.PostConstruct;
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

import com.google.common.collect.Iterables;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pw.ddm.platform.core.util.IdGenerator;

@Slf4j
@Service
class LocalDataLoader implements DataLoader {

    @Value("${paths.datasets}")
    private String datasetsPath;

    private final Map<String, DataDesc> dataMap = new HashMap<>();

    @SneakyThrows
    @Override
    public String save(@NonNull String uri, String separator, Integer idIndex, Integer labelIndex,
            DataOptions dataOptions) {
        var id = IdGenerator.generate(uri);
        var name = uri.substring(FilenameUtils.indexOfLastSeparator(uri) + 1);

        // TODO improve without copy
        var fileCreator = new TempFileCreator();
        var file = fileCreator.create(name)
                .toFile();
        FileUtils.copyURLToFile(new URL(uri), file);
        var data = saveAndPrepareDataDesc(id, FileUtils.readFileToByteArray(file), name, separator, idIndex, labelIndex,
                dataOptions);

        updateDataMap(id, data);
        fileCreator.cleanup();

        return id;
    }

    // TODO prepare dto for response
    @SneakyThrows
    @Override
    public List<String> saveExtractTrain(@NonNull String uri, String separator, Integer idIndex, Integer labelIndex,
            DataOptions dataOptions) {
        var trainId = IdGenerator.generate(uri + TypeCode.TRAIN.name());
        var testId = IdGenerator.generate(uri + TypeCode.TEST.name());
        var name = uri.substring(FilenameUtils.indexOfLastSeparator(uri) + 1);

        // TODO improve without copy
        var fileCreator = new TempFileCreator();
        var path = fileCreator.create(name);
        FileUtils.copyURLToFile(new URL(uri), path.toFile());

        var extractor = new LocalDatasetTrainExtractor(path, separator, labelIndex, dataOptions);
        extractor.extract();
        var trainFile = extractor.getTrainFile()
                .toFile();
        var testFile = extractor.getTestFile()
                .toFile();

        var now = System.currentTimeMillis();
        var trainName = name + TypeCode.TRAIN.name() + now;
        var testName = name + TypeCode.TEST.name() + now;
        var trainData =
                saveAndPrepareDataDesc(trainId, FileUtils.readFileToByteArray(trainFile), trainName, separator, idIndex,
                        labelIndex, dataOptions);
        var testData =
                saveAndPrepareDataDesc(testId, FileUtils.readFileToByteArray(testFile), testName, separator, idIndex,
                        labelIndex, dataOptions);

        // TODO FIXME not necessarily cause of seed
        updateDataMap(trainId, trainData);
        updateDataMap(testId, testData);
        extractor.cleanup();
        fileCreator.cleanup();

        return List.of(trainId, testId);
    }

    @SneakyThrows
    @Override
    public String save(MultipartFile file, String separator, Integer idIndex, Integer labelIndex,
            DataOptions dataOptions) {
        var id = IdGenerator.generate(file.getOriginalFilename() + file.getSize());
        DataDesc data =
                saveAndPrepareDataDesc(id, file.getBytes(), file.getOriginalFilename(), separator, idIndex, labelIndex,
                        dataOptions);
        updateDataMap(id, data);

        return id;
    }

    // TODO prepare dto for response
    @SneakyThrows
    @Override
    public List<String> saveExtractTrain(MultipartFile file, String separator, Integer idIndex, Integer labelIndex,
            DataOptions dataOptions) {
        var uri = file.getOriginalFilename();
        var name = uri.substring(FilenameUtils.indexOfLastSeparator(uri) + 1);
        var trainId = IdGenerator.generate(name + TypeCode.TRAIN.name());
        var testId = IdGenerator.generate(name + TypeCode.TEST.name());

        // TODO improve without copy
        var fileCreator = new TempFileCreator();
        var path = fileCreator.create(name);
        FileUtils.copyToFile(file.getInputStream(), path.toFile());

        var extractor = new LocalDatasetTrainExtractor(path, separator, labelIndex, dataOptions);
        extractor.extract();
        var trainFile = extractor.getTrainFile()
                .toFile();
        var testFile = extractor.getTestFile()
                .toFile();

        var now = System.currentTimeMillis();
        var trainName = name + TypeCode.TRAIN.name() + now;
        var testName = name + TypeCode.TEST.name() + now;
        var trainData =
                saveAndPrepareDataDesc(trainId, FileUtils.readFileToByteArray(trainFile), trainName, separator, idIndex,
                        labelIndex, dataOptions);
        var testData =
                saveAndPrepareDataDesc(testId, FileUtils.readFileToByteArray(testFile), testName, separator, idIndex,
                        labelIndex, dataOptions);

        // TODO FIXME not necessarily cause of seed
        updateDataMap(trainId, trainData);
        updateDataMap(testId, testData);
        fileCreator.cleanup();

        return List.of(trainId, testId);
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

    private DataDesc saveAndPrepareDataDesc(String id, byte[] bytes, String name, String separator, Integer idIndex,
            Integer labelIndex, DataOptions dataOptions) throws IOException {
        // TODO check type inside file
        String type = FilenameUtils.getExtension(name);
        Path dataPath = Paths.get(datasetsPath, id + "." + type);
        Files.write(dataPath, bytes);

        boolean addIndex = idIndex == null;
        new LocalDatasetProcessor(addIndex, dataOptions.isVectorizeStrings(), dataPath, separator, idIndex,
                labelIndex).process();
        if (addIndex) {
            idIndex = 0;
            ++labelIndex;
        }

        return new DataDescriber(dataPath, id, name, separator, idIndex, labelIndex, dataOptions.isDeductType(),
                !dataOptions.isDeductType()) // TODO FIXME force all numeric
                .describe();
    }

    private void updateDataMap(String dataId, DataDesc dataDesc) {
        if (dataMap.put(dataId, dataDesc) != null) {
            log.warn("Loaded probably the same data as before with id '{}'.", dataId);
        }
    }

    @PostConstruct
    void init() throws IOException {
        // TODO save on PreDestroy and collect or keep removed
        Files.createDirectories(Paths.get(datasetsPath));
    }

    @PreDestroy
    void destroy() {
        // TODO disable for persistent config
        log.info("PreDestroy " + this.getClass()
                .getSimpleName());
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
