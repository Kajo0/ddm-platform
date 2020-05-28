package pl.edu.pw.ddm.platform.testing.interfaces.impl.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;

@RequiredArgsConstructor
public class NodeDataProvider implements DataProvider {

    @NonNull
    private final String dataPath;

    private final String testDataPath;

    @NonNull
    private final String separator;

    @NonNull
    private final Integer idIndex;

    @NonNull
    private final Integer labelIndex;

    @NonNull
    private final Integer attributesAmount;

    @NonNull
    private final String[] colTypes;

    private String[] attributesColTypes;

    private Collection<Data> trainingSet;
    private Collection<Data> testSet;
    private Collection<Data> allSet;

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

    private String[] getAttributesColTypes() {
        if (attributesColTypes != null) {
            return attributesColTypes;
        }

        attributesColTypes = new String[attributesAmount];
        for (int i = 0, j = 0; i < colTypes.length; ++i) {
            if (i != idIndex && i != labelIndex) {
                attributesColTypes[j++] = colTypes[i];
            }
        }

        return attributesColTypes;
    }

    private void loadTraining() {
        trainingSet = loadCsvData(dataPath);
    }

    private void loadTest() {
        testSet = loadCsvData(testDataPath);
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
                .map(l -> toArray(l, separator))
                .map(this::toNodeData)
                .collect(Collectors.toList());
    }

    private String[] toArray(String line, String separator) {
        return line.split(separator);
    }

    private NodeData toNodeData(String[] values) {
        String[] attributes = new String[getAttributesColTypes().length];
        for (int i = 0, j = 0; i < values.length; ++i) {
            if (i != idIndex && i != labelIndex) {
                attributes[j++] = values[i];
            }
        }

        String id = values[idIndex];
        String label = labelIndex != null ? values[labelIndex] : null;

        return new NodeData(String.valueOf(id), String.valueOf(label), attributes, getAttributesColTypes());
    }

}
