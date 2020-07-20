package ddm.samples;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.SneakyThrows;
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;

public class NodeDataProvider implements DataProvider {

    @NonNull
    private final String dataPath;
    private final String testDataPath;
    private final DataDesc dataDesc;

    private Collection<Data> trainingSet;
    private Collection<Data> testSet;
    private Collection<Data> allSet;

    public NodeDataProvider(@NonNull String dataPath, String testDataPath, @NonNull DataDesc dataDesc) {
        this.dataPath = dataPath;
        this.testDataPath = testDataPath;
        this.dataDesc = dataDesc;
    }

    @Override
    public DataDesc getDataDescription() {
        return dataDesc;
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

    private void loadTraining() {
        trainingSet = dataPath == null ? Collections.emptyList() : loadCsvData(dataPath);
    }

    private void loadTest() {
        testSet = testDataPath == null ? Collections.emptyList() : loadCsvData(testDataPath);
    }

    private void loadAll() {
        loadTraining();
        loadTest();
        allSet = new LinkedList<>();
        allSet.addAll(trainingSet);
        allSet.addAll(testSet);
    }

    @SneakyThrows
    private Collection<Data> loadCsvData(String file) {
        Path path = Paths.get(file);
        if (Files.notExists(path)) {
            return Collections.emptyList();
        }

        return Files.readAllLines(path)
                .stream()
                .map(l -> toArray(l, dataDesc.getSeparator()))
                .map(this::toNodeData)
                .collect(Collectors.toList());
    }

    private String[] toArray(String line, String separator) {
        return line.split(separator);
    }

    private NodeData toNodeData(String[] values) {
        String[] attributes = new String[dataDesc.getAttributesColTypes().length];
        for (int i = 0, j = 0; i < values.length; ++i) {
            if (i != dataDesc.getIdIndex() && i != dataDesc.getLabelIndex()) {
                attributes[j++] = values[i];
            }
        }

        String id = values[dataDesc.getIdIndex()];
        String label = dataDesc.getLabelIndex() != null ? values[dataDesc.getLabelIndex()] : null;

        return new NodeData(String.valueOf(id), String.valueOf(label), attributes, dataDesc.getAttributesColTypes());
    }

}
