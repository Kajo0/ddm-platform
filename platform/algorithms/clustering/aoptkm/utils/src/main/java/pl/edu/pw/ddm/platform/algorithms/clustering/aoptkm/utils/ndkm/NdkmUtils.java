package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.ndkm;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import com.google.common.collect.Lists;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.point.DoublePoint;

public class NdkmUtils {

    /**
     * Always 2 elements or throw error
     *
     * @return [{minX, minY}, {maxX, maxY}]
     */
    public static DoublePoint[] findLocalMinMax(List<DoublePoint> points) {
        checkState(!points.isEmpty());
        int length = points.get(0).values.length;
        double[] max = new double[length];
        double[] min = new double[length];
        for (int i = 0; i < length; ++i) {
            max[i] = Double.MAX_VALUE;
            min[i] = Double.MIN_VALUE;
        }
        DoublePoint[] minMax = new DoublePoint[]{new DoublePoint(max), new DoublePoint(min)};

        points.stream().forEach(p -> {
            for (int i = 0; i < length; ++i) {
                minMax[0].values[i] = Math.min(minMax[0].values[i], p.values[i]);
                minMax[1].values[i] = Math.max(minMax[1].values[i], p.values[i]);
            }
        });

        return minMax;
    }

    public static DoublePoint[] findGlobalMinMax(List<DoublePoint[]> points) {
        checkState(!points.isEmpty());
        int length = points.get(0)[0].values.length;
        double[] max = new double[length];
        double[] min = new double[length];
        for (int i = 0; i < length; ++i) {
            max[i] = Double.MAX_VALUE;
            min[i] = Double.MIN_VALUE;
        }
        DoublePoint[] minMax = new DoublePoint[]{new DoublePoint(max), new DoublePoint(min)};

        points.stream().forEach(p -> {
            for (int i = 0; i < p.length; ++i) {
                for (int j = 0; j < length; ++j) {
                    minMax[0].values[j] = Math.min(minMax[0].values[j], p[i].values[j]);
                    minMax[1].values[j] = Math.max(minMax[1].values[j], p[i].values[j]);
                }
            }
        });

        return minMax;
    }

    public static List<DoublePoint> normalizeMinMax(List<DoublePoint> points, DoublePoint min, DoublePoint max) {
        List<DoublePoint> normalized = Lists.newArrayList();

        final double diffX = max.values[0] - min.values[0];
        final double diffY = max.values[1] - min.values[1];
        points.stream().forEach(p -> {
            double[] val = new double[2];
            val[0] = (p.values[0] - min.values[0]) / (diffX);
            val[1] = (p.values[1] - min.values[1]) / (diffY);
            normalized.add(new DoublePoint(val, p.index));
        });

        return normalized;
    }

}
