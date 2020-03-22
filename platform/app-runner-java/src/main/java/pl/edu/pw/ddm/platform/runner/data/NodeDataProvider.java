package pl.edu.pw.ddm.platform.runner.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.SneakyThrows;
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;

public class NodeDataProvider implements DataProvider {

    private static final String TRAINING_DATA = "/execution/training_data.txt";
    private static final String TEST_DATA = "/execution/test_data.txt";
    private static final String SEPARATOR = ",";

    // TODO use dataId
    private final String dataId;
    private Collection<Data> trainingSet;
    private Collection<Data> testSet;
    private Collection<Data> allSet;

    public NodeDataProvider(String dataId) {
        this.dataId = dataId;
    }

    public void loadTraining() {
        trainingSet = loadCsvData(TRAINING_DATA);
    }

    public void loadTest() {
        testSet = loadCsvData(TEST_DATA);
    }

    public void loadAll() {
        // TODO load
        allSet = new LinkedList<>();
        allSet = loadDummy();
    }

    @Override
    public Collection<Data> training() {
        return trainingSet;
    }

    @Override
    public Collection<Data> test() {
        return testSet;
    }

    @Override
    public Collection<Data> all() {
        return allSet;
    }


    // TODO remove
    private Collection<Data> loadDummy() {
        return IntStream.range(0, Integer.parseInt(dataId))
                .mapToObj(i -> new NodeData(i + ".", "-" + i, new double[]{i, i}))
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private Collection<Data> loadCsvData(String file) {
        // TODO improve loading data
        Path path = Paths.get(file);
        return Files.readAllLines(path)
                .stream()
                .map(this::toDoubleArray)
                .map(attrs -> new NodeData("" + attrs[0], "" + attrs[1], Arrays.copyOfRange(attrs, 2, attrs.length)))
                .collect(Collectors.toList());
    }

    private double[] toDoubleArray(String line) {
        String[] attrs = line.split(SEPARATOR);
        double[] result = new double[attrs.length];
        for (int i = 0; i < attrs.length; ++i) {
            result[i] = Double.parseDouble(attrs[i]);
        }
        return result;
    }

}
