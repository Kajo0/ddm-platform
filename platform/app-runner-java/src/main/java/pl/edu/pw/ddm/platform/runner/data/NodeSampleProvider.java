package pl.edu.pw.ddm.platform.runner.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.SampleData;
import pl.edu.pw.ddm.platform.interfaces.data.SampleProvider;

public class NodeSampleProvider implements SampleProvider {

    private final Collection<SampleData> data;
    private final Iterator<SampleData> iterator;

    public NodeSampleProvider(Collection<NodeData> data) {
        this.data = data.stream()
                .map(NodeData::toSample)
                .collect(Collectors.toList());
        this.iterator = this.data.iterator();
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

    @Override
    public Collection<SampleData> all() {
        return data;
    }

}
