package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.distance;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

/**
 * Impl. of Euclidean/Hamming function for numeric or categorical data
 */
public class EuclideanHammingFunction implements DistanceFunction<Object[]> {

    @Override
    public double distance(Object[] first, Object[] second) {
        if (first.length != second.length) {
            throw new IllegalArgumentException("First length != second length (" + first.length + "!=" + second.length + ")");
        }

        double numeric = 0;
        int categorical = 0;

        for (int i = 0; i < first.length; ++i) {
            Object f = first[i];
            Object s = second[i];

            if (!f.getClass().equals(s.getClass())) {
                throw new IllegalArgumentException();
            } else if (f instanceof Number && s instanceof Number) {
                numeric += Math.pow(((Number) f).doubleValue() - ((Number) s).doubleValue(), 2.0);
            } else {
                categorical += f.equals(s) ? 0 : 1;
            }
        }

        return Math.sqrt(numeric) + categorical;
    }

    @Override
    public Object[] meanMerge(List<Object[]> list) {
        Preconditions.checkState(!list.isEmpty());
        Object[] first = list.get(0);
        Object[] values = new Object[first.length];

        for (int i = 0; i < values.length; ++i) {
            int finalI = i;
            if (first[i] instanceof Number) {
                double v = list.stream().map(val -> ((Number) val[finalI]).doubleValue()).reduce(0d, (a, b) -> a + b);
                values[i] = v / list.size();
            } else {
                values[i] = list.stream()
                        .map(val -> val[finalI])
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                        .entrySet()
                        .stream()
                        .max(Comparator.comparing(Map.Entry::getValue))
                        .map(Map.Entry::getKey)
                        .orElse(null);
            }
        }

        return values;
    }

}
