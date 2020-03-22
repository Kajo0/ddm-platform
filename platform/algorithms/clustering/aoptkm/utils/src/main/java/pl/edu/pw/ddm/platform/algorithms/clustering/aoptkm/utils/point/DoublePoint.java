package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.point;

import java.util.Arrays;

import lombok.EqualsAndHashCode;

/**
 * Impl. of point for double[] values
 */
@EqualsAndHashCode(callSuper = true)
public class DoublePoint extends Point<double[]> {

    /**
     * {@inheritDoc}
     */
    public DoublePoint(double[] values, Integer index) {
        super(values, index);
    }

    /**
     * {@inheritDoc}
     */
    public DoublePoint(double[] values) {
        super(values);
    }

    @Override
    public String toString() {
        return String.format("[%s v=%s]", super.toString(), Arrays.toString(values));
    }

}
