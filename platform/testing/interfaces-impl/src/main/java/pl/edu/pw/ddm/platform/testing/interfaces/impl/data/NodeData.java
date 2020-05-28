package pl.edu.pw.ddm.platform.testing.interfaces.impl.data;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.NotImplementedException;
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.SampleData;

@ToString(exclude = "numericAttributes")
@RequiredArgsConstructor
public class NodeData implements Data {

    private final String id;
    private final String label;
    private final String[] attributes;
    private final String[] colTypes;

    protected double[] numericAttributes;

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
        return attributes;
    }

    @Override
    public double[] getNumericAttributes() {
        if (numericAttributes != null) {
            return numericAttributes;
        }

        long numericCount = Stream.of(colTypes)
                .filter(Objects::nonNull)
                .filter("numeric"::equals)
                .count();
        if (numericCount != attributes.length) {
            throw new IllegalArgumentException("Not all but " + numericCount + " attributes of " + attributes.length + " are numeric: " + Arrays.toString(colTypes));
        }

        numericAttributes = new double[attributes.length];
        for (int i = 0; i < attributes.length; ++i) {
            numericAttributes[i] = Double.parseDouble(attributes[i]);
        }
        return numericAttributes;
    }

    @Override
    public String getAttribute(int col) {
        return attributes[col];
    }

    @Override
    public String getAttribute(String name) {
        throw new NotImplementedException("Not implemented yet.");
    }

    @Override
    public double getNumericAttribute(int col) {
        if (numericAttributes != null) {
            return numericAttributes[col];
        } else if ("numeric".equals(colTypes[col])) {
            return Double.parseDouble(getAttribute(col));
        } else {
            throw new IllegalArgumentException("Data column " + col + " is not numeric.");
        }
    }

    @Override
    public double getNumericAttribute(String name) {
        throw new NotImplementedException("Not implemented yet.");
    }

    SampleData toSample() {
        return new NodeSampleData(id, label, attributes, colTypes, numericAttributes);
    }

}
