package pl.edu.pw.ddm.platform.runner.data;

import pl.edu.pw.ddm.platform.interfaces.data.SampleData;

public class NodeSampleData extends NodeData implements SampleData {

    public NodeSampleData(String id, String label, String[] attributes, String[] colTypes, double[] numericAttributes) {
        super(id, label, attributes, colTypes);
        this.numericAttributes = numericAttributes;
    }

    @Override
    SampleData toSample() {
        throw new UnsupportedOperationException("Unsupported operation");
    }

}
