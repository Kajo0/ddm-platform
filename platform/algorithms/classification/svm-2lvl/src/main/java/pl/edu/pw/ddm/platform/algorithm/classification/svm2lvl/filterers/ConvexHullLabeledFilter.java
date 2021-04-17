package pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.filterers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.utils.LabeledObservation;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;

@RequiredArgsConstructor
public class ConvexHullLabeledFilter {

    private final DistanceFunction distanceFunction;
    private final Collection<LabeledObservation> training;

    public Collection<LabeledObservation> process() {
        Set<LabeledNodeData> data = training.stream()
                .map(LabeledNodeData::new)
                .collect(Collectors.toSet());
        LabeledNodeData min = min(data);
        LabeledNodeData max = max(data);

        Pair<Set<LabeledNodeData>> leftRight = leftAndRight(data, min, max);

        Set<LabeledObservation> convex = new HashSet<>();
        convex.add(min.getLabeled());
        convex.add(max.getLabeled());
        quickHull(min, max, leftRight.second).stream()
                .map(LabeledNodeData::getLabeled)
                .forEach(convex::add);
        quickHull(max, min, leftRight.first).stream()
                .map(LabeledNodeData::getLabeled)
                .forEach(convex::add);
        return convex;
    }

    private static Set<LabeledNodeData> quickHull(LabeledNodeData a, LabeledNodeData b, Set<LabeledNodeData> data) {
        if (data.isEmpty()) {
            return Collections.emptySet();
        }
        LabeledNodeData farthest = farthest(a, b, data);

        Set<LabeledNodeData> set = new HashSet<>();
        set.add(farthest);

        Set<LabeledNodeData> s1 = right(data, a, farthest);
        Set<LabeledNodeData> s2 = right(data, farthest, b);

        set.addAll(quickHull(a, farthest, s1));
        set.addAll(quickHull(farthest, b, s2));

        return set;
    }

    private static LabeledNodeData farthest(LabeledNodeData a, LabeledNodeData b, Set<LabeledNodeData> data) {
        Map<LabeledNodeData, Double> map = new HashMap<>();
//        data.forEach(d -> map.put(d,
//                distanceFunction.distance(d.getLabeled(), a.getLabeled()) + distanceFunction.distance(d.getLabeled(),
//                        b.getLabeled())));
        data.forEach(d -> map.put(d, calc(a, b, d)));
        return map.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(RuntimeException::new);
    }

    private static Set<LabeledNodeData> right(Set<LabeledNodeData> data, LabeledNodeData min, LabeledNodeData max) {
        Map<LabeledNodeData, Double> map = new HashMap<>();
        data.forEach(d -> map.put(d, calc(min, max, d)));

        return map.entrySet()
                .stream()
                .filter(e -> e.getValue() >= 0)
                .filter(e -> e.getKey() != min)
                .filter(e -> e.getKey() != max)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private static Pair<Set<LabeledNodeData>> leftAndRight(Set<LabeledNodeData> data, LabeledNodeData min,
            LabeledNodeData max) {
        Map<LabeledNodeData, Double> map = new HashMap<>();
        data.forEach(d -> map.put(d, calc(min, max, d)));

        Set<LabeledNodeData> right = map.entrySet()
                .stream()
                .filter(e -> e.getValue() >= 0)
                .filter(e -> e.getKey() != min)
                .filter(e -> e.getKey() != max)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        Set<LabeledNodeData> left = map.entrySet()
                .stream()
                .filter(e -> e.getValue() < 0)
                .filter(e -> e.getKey() != min)
                .filter(e -> e.getKey() != max)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        return new Pair<>(left, right);
    }

    private static double calc(LabeledNodeData a, LabeledNodeData b, LabeledNodeData o) {
        double[] ad = a.getLabeled()
                .getFeatures();
        double[] bd = b.getLabeled()
                .getFeatures();
        double[] od = o.getLabeled()
                .getFeatures();
        double result = 0;

        int length = ad.length;
        for (int i = 0, j = 1; j < length; ++i, ++j) {
            result += calcC(ad[i], ad[j], bd[i], bd[j], od[i], od[j]);
        }

        return result;
    }

    private static double calcC(double ax, double ay, double bx, double by, double ox, double oy) {
        return (ax - ox) * (by - oy) - (ay - oy) * (bx - ox);
    }

    private static LabeledNodeData min(Set<LabeledNodeData> data) {
        return data.stream()
                .min(LabeledNodeData::compareTo)
                .orElseThrow(RuntimeException::new);
    }

    private static LabeledNodeData max(Set<LabeledNodeData> data) {
        return data.stream()
                .max(LabeledNodeData::compareTo)
                .orElseThrow(RuntimeException::new);
    }

    @Value
    static class Pair<T> {

        T first;
        T second;
    }

}
