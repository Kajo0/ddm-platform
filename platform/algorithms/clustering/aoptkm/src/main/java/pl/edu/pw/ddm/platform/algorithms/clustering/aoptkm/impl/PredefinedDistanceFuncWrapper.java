package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.impl;

import java.util.List;
import java.util.Optional;

import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.distance.DistanceFunction;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.distance.EuclideanHammingFunction;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;

public class PredefinedDistanceFuncWrapper implements DistanceFunction<Object[]> {

    private final pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction distanceFunc;
    private final DistanceFunction<Object[]> defaultDistanceFunc;

    public PredefinedDistanceFuncWrapper(ParamProvider paramProvider) {
        this.distanceFunc = paramProvider.distanceFunction();
        this.defaultDistanceFunc = new EuclideanHammingFunction();
    }

    @Override
    public double distance(Object[] first, Object[] second) {
        if (distanceFunc != null) {
            return distanceFunc.distance(toStringArray(first), toStringArray(second));
        } else {
            return defaultDistanceFunc.distance(first, second);
        }
    }

    @Override
    public Object[] meanMerge(List<Object[]> list) {
        return defaultDistanceFunc.meanMerge(list);
    }

    private String[] toStringArray(Object[] arr) {
        String[] result = new String[arr.length];
        for (int i = 0; i < arr.length; ++i) {
            result[i] = arr[i] == null ? null : String.valueOf(arr[i]);
        }
        return result;
    }

    @Override
    public String toString() {
        return Optional.ofNullable(distanceFunc)
                .map(pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction::name)
                .orElseGet(defaultDistanceFunc::toString);
    }

}
