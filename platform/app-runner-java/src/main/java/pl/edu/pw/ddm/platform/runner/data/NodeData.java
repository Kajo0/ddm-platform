package pl.edu.pw.ddm.platform.runner.data;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.SampleData;

@AllArgsConstructor
public class NodeData implements Data {

    private final String id;
    private final String label;
    private final double[] attributes;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String[] getAttributes() {
        // TODO implement
        throw new NotImplementedException("Not implemented yet.");
    }

    @Override
    public double[] getNumericAttributes() {
        return attributes;
    }

    @Override
    public String getAttribute(int col) {
        // TODO implement
        throw new NotImplementedException("Not implemented yet.");
    }

    @Override
    public String getAttribute(String name) {
        // TODO implement
        throw new NotImplementedException("Not implemented yet.");
    }

    @Override
    public double getNumericAttribute(int col) {
        return attributes[col];
    }

    @Override
    public double getNumericAttribute(String name) {
        // TODO implement
        throw new NotImplementedException("Not implemented yet.");
    }

    SampleData toSample() {
        // TODO think about purpose
        return new NodeSampleData(id, label, attributes);
    }

}
