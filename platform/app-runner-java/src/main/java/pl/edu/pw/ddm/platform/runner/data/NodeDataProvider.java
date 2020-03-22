package pl.edu.pw.ddm.platform.runner.data;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;

public class NodeDataProvider implements DataProvider {

    private final String dataId;
    private Collection<Data> trainingSet;
    private Collection<Data> testSet;
    private Collection<Data> allSet;

    public NodeDataProvider(String dataId) {
        this.dataId = dataId;
    }

    // TODO remove
    private Collection<Data> loadDummy() {
        return IntStream.range(0, Integer.parseInt(dataId))
                .mapToObj(i -> new NodeData(i + ".", "-" + i, new double[]{i, i}))
                .collect(Collectors.toList());
    }

    public void loadTraining() {
        // TODO load
        trainingSet = new LinkedList<>();
        trainingSet = loadDummy();
    }

    public void loadTest() {
        // TODO load
        testSet = new LinkedList<>();
        testSet = loadDummy();
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

}
