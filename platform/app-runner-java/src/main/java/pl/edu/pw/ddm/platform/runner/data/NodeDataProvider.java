package pl.edu.pw.ddm.platform.runner.data;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.collections.CollectionUtils;
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;

public class NodeDataProvider implements DataProvider {

    private final String dataPath;
    // TODO think about, both might be same named as they have different ids
    private final String trainDataId;
    private final String testDataId;
    private final DataDesc trainDataDesc;
    private final DataDesc testDataDesc;

    private Collection<Data> trainingSet;
    private Collection<Data> testSet;
    private Collection<Data> allSet;

    @Getter
    private long loadingMillis;

    public NodeDataProvider(String dataPath, String trainDataId, String testDataId) {
        this.dataPath = dataPath;
        this.trainDataId = trainDataId;
        this.trainDataDesc = loadDescription(trainDataId);
        this.testDataId = testDataId;
        if (testDataId != null) {
            this.testDataDesc = loadDescription(testDataId);
        } else {
            this.testDataDesc = null;
        }
    }

    @Override
    public DataDesc getDataDescription() {
        return trainDataDesc;
    }

    @Override
    public Collection<Data> training() {
        if (trainingSet == null) {
            loadTraining();
        }
        return trainingSet;
    }

    @Override
    public Collection<Data> test() {
        if (testSet == null) {
            loadTest();
        }
        return testSet;
    }

    @Override
    public Collection<Data> all() {
        if (allSet == null) {
            loadAll();
        }
        return allSet;
    }

    public Integer trainingSize() {
        if (trainingSet != null) {
            return trainingSet.size();
        } else if (allSet != null) {
            return allSet.size();
        } else {
            // to avoid loading
            return null;
        }
    }

    public List<Data> trainingSample10() {
        if (CollectionUtils.isNotEmpty(trainingSet)) {
            return trainingSet.stream()
                    .limit(10)
                    .collect(Collectors.toList());
        } else if (CollectionUtils.isNotEmpty(allSet)) {
            return allSet.stream()
                    .limit(10)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @SneakyThrows
    private DataDesc loadDescription(String dataId) {
        Properties prop = new Properties();
        File file = Paths.get(dataPath + "/" + dataId + "/desc").toFile();
        try (FileInputStream fis = new FileInputStream(file)) {
            prop.load(fis);
        }

        return DataDesc.builder()
                .separator(prop.getProperty("separator"))
                .idIndex(Integer.valueOf(prop.getProperty("idIndex")))
                .labelIndex(Integer.valueOf(prop.getProperty("labelIndex")))
                .attributesAmount(Integer.valueOf(prop.getProperty("attributesAmount")))
                .colTypes(prop.getProperty("colTypes").split(","))
                .build();
    }

    private void loadTraining() {
        long start = System.currentTimeMillis();

        trainingSet = loadCsvData(dataPath + "/" + trainDataId + "/train", trainDataDesc);

        long end = System.currentTimeMillis();
        loadingMillis += end - start;
    }

    private void loadTest() {
        long start = System.currentTimeMillis();

        testSet = loadCsvData(dataPath + "/" + testDataId + "/test", testDataDesc);

        long end = System.currentTimeMillis();
        loadingMillis += end - start;
    }

    private void loadAll() {
        // TODO check if same types
        loadTraining();
        loadTest();
        allSet = new LinkedList<>();
        allSet.addAll(trainingSet);
        allSet.addAll(testSet);
    }

    @SneakyThrows
    private Collection<Data> loadCsvData(String file, DataDesc dataDesc) {
        // TODO improve loading data
        Path path = Paths.get(file);
        if (Files.notExists(path)) {
            // TODO log
            return Collections.emptyList();
        }

        String[] attrColTypes = dataDesc.getAttributesColTypes();
        return Files.readAllLines(path)
                .stream()
                .map(l -> toArray(l, dataDesc.getSeparator()))
                .map(values -> toNodeData(values, attrColTypes, dataDesc))
                .collect(Collectors.toList());
    }

    private String[] toArray(String line, String separator) {
        return line.split(separator);
    }

    private NodeData toNodeData(String[] values, String[] attrColTypes, DataDesc dataDesc) {
        // TODO Array.copy as label always will be placed at the end such as index on the first place
        String[] attributes = new String[dataDesc.getAttributesAmount()];
        for (int i = 0, j = 0; i < values.length; ++i) {
            if (i != dataDesc.getIdIndex() && i != dataDesc.getLabelIndex()) {
                attributes[j++] = values[i];
            }
        }

        String id = values[dataDesc.getIdIndex()];
        String label = dataDesc.getLabelIndex() != null ? values[dataDesc.getLabelIndex()] : null;

        return new NodeData(String.valueOf(id), String.valueOf(label), attributes, attrColTypes);
    }

}
