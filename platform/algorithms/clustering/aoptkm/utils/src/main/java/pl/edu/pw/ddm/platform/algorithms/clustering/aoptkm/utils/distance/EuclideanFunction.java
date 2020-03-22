package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.distance;

import java.util.List;

import com.google.common.base.Preconditions;

/**
 * Impl. of Euclidean function for double[] points
 */
public class EuclideanFunction implements DistanceFunction<double[]> {

    @Override
    public double distance(double[] first, double[] second) {
        if (first.length != second.length) {
            throw new IllegalArgumentException();
        }

        double ret = 0;

        for (int i = 0; i < first.length; ++i) {
            ret += Math.pow(first[i] - second[i], 2.0);
        }

        return Math.sqrt(ret);
    }

    @Override
    public double[] meanMerge(List<double[]> list) {
        Preconditions.checkState(!list.isEmpty());
        double[] values = new double[list.get(0).length];

        list.forEach(val -> {
            for (int i = 0; i < values.length; ++i) {
                values[i] += val[i];
            }
        });
        for (int i = 0; i < values.length; ++i) {
            values[i] /= list.size();
        }

        return values;
    }

}
