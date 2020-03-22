package pl.edu.pw.ddm.platform.runner.data;

import pl.edu.pw.ddm.platform.interfaces.data.SampleData;

public class NodeSampleData extends NodeData implements SampleData {

    public NodeSampleData(String id, String label, double[] attributes) {
        super(id, label, attributes);
    }

    @Override
    SampleData toSample() {
        throw new UnsupportedOperationException("Unsupported operation");
    }

}
