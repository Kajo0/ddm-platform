package pl.edu.pw.ddm.platform.runner.data;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;

public class NodeDataProvider implements DataProvider {

    private static final String DATA_PATH = "/ddm/data/";

    private final String dataId;
    private final DataDesc dataDesc;

    private Collection<Data> trainingSet;
    private Collection<Data> testSet;
    private Collection<Data> allSet;

    public NodeDataProvider(String dataId) {
        this.dataId = dataId;
        this.dataDesc = loadDescription();
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

    @SneakyThrows
    private DataDesc loadDescription() {
        Properties prop = new Properties();
        File file = Paths.get(DATA_PATH + dataId + "/desc").toFile();
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
        trainingSet = loadCsvData(DATA_PATH + dataId + "/train");
    }

    private void loadTest() {
        // TODO change to test
//        testSet = loadCsvData(DATA_PATH + dataId + "/test");
        testSet = loadCsvData(DATA_PATH + dataId + "/train");
    }

    private void loadAll() {
        // TODO load
        loadTraining();
        loadTest();
        allSet = new LinkedList<>();
        allSet.addAll(trainingSet);
        allSet.addAll(testSet);
    }

    @SneakyThrows
    private Collection<Data> loadCsvData(String file) {
        // TODO improve loading data
        Path path = Paths.get(file);
        if (Files.notExists(path)) {
            // TODO log
            return Collections.emptyList();
        }

        String[] attrColTypes = dataDesc.getAttributesColTypes();
        return Files.readAllLines(path)
                .stream()
                .map(this::toArray)
                .map(values -> toNodeData(values, attrColTypes))
                .collect(Collectors.toList());
    }

    private String[] toArray(String line) {
        return line.split(dataDesc.getSeparator());
    }

    private NodeData toNodeData(String[] values, String[] attrColTypes) {
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
