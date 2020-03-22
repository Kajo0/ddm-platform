package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.point;

import java.util.Arrays;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class ObjectPoint extends Point<Object[]> {

    public ObjectPoint(Object[] values, Integer index) {
        super(values, index);
    }

    public ObjectPoint(Object[] values) {
        super(values);
    }

    @Override
    public String toString() {
        return String.format("[%s v=%s]", super.toString(), Arrays.toString(values));
    }

}
