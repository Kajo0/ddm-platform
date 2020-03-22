package pl.edu.pw.ddm.platform.runner.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.SampleData;
import pl.edu.pw.ddm.platform.interfaces.data.SampleProvider;

public class NodeSampleProvider implements SampleProvider {

    private final Iterator<SampleData> iterator;

    public NodeSampleProvider(Collection<NodeData> data) {
        this.iterator = data.stream()
                .map(NodeData::toSample)
                .iterator();
    }

    public static NodeSampleProvider fromData(Collection<Data> data) {
        return data.stream()
                .map(d -> (NodeData) d)
                .collect(Collectors.collectingAndThen(Collectors.toList(), NodeSampleProvider::new));
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public SampleData next() {
        return iterator.next();
    }

}
