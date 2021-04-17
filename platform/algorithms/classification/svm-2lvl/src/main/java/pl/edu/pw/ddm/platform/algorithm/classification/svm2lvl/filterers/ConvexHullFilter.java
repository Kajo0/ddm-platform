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
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;

@RequiredArgsConstructor
public class ConvexHullFilter {

    private final DistanceFunction distanceFunction;
    private final Collection<Data> training;

    public Collection<Data> process() {
        Set<NodeData> data = training.stream()
                .map(NodeData::new)
                .collect(Collectors.toSet());
        NodeData min = min(data);
        NodeData max = max(data);

        Pair<Set<NodeData>> leftRight = leftAndRight(data, min, max);

        Set<Data> convex = new HashSet<>();
        convex.add(min.getData());
        convex.add(max.getData());
        quickHull(min, max, leftRight.second).stream()
                .map(NodeData::getData)
                .forEach(convex::add);
        quickHull(max, min, leftRight.first).stream()
                .map(NodeData::getData)
                .forEach(convex::add);
        return convex;
    }

    private static Set<NodeData> quickHull(NodeData a, NodeData b, Set<NodeData> data) {
        if (data.isEmpty()) {
            return Collections.emptySet();
        }
        NodeData farthest = farthest(a, b, data);

        Set<NodeData> set = new HashSet<>();
        set.add(farthest);

        Set<NodeData> s1 = right(data, a, farthest);
        Set<NodeData> s2 = right(data, farthest, b);

        set.addAll(quickHull(a, farthest, s1));
        set.addAll(quickHull(farthest, b, s2));

        return set;
    }

    private static NodeData farthest(NodeData a, NodeData b, Set<NodeData> data) {
        Map<NodeData, Double> map = new HashMap<>();
//        data.forEach(d -> map.put(d,
//                distanceFunction.distance(d.getData(), a.getData()) + distanceFunction.distance(d.getData(),
//                        b.getData())));
        data.forEach(d -> map.put(d, calc(a, b, d)));
        return map.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(RuntimeException::new);
    }

    private static Set<NodeData> right(Set<NodeData> data, NodeData min, NodeData max) {
        Map<NodeData, Double> map = new HashMap<>();
        data.forEach(d -> map.put(d, calc(min, max, d)));

        return map.entrySet()
                .stream()
                .filter(e -> e.getValue() >= 0)
                .filter(e -> e.getKey() != min)
                .filter(e -> e.getKey() != max)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private static Pair<Set<NodeData>> leftAndRight(Set<NodeData> data, NodeData min, NodeData max) {
        Map<NodeData, Double> map = new HashMap<>();
        data.forEach(d -> map.put(d, calc(min, max, d)));

        Set<NodeData> right = map.entrySet()
                .stream()
                .filter(e -> e.getValue() >= 0)
                .filter(e -> e.getKey() != min)
                .filter(e -> e.getKey() != max)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        Set<NodeData> left = map.entrySet()
                .stream()
                .filter(e -> e.getValue() < 0)
                .filter(e -> e.getKey() != min)
                .filter(e -> e.getKey() != max)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        return new Pair<>(left, right);
    }

    private static double calc(NodeData a, NodeData b, NodeData o) {
        double[] ad = a.getData()
                .getNumericAttributes();
        double[] bd = b.getData()
                .getNumericAttributes();
        double[] od = o.getData()
                .getNumericAttributes();
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

    private static NodeData min(Set<NodeData> data) {
        return data.stream()
                .min(NodeData::compareTo)
                .orElseThrow(RuntimeException::new);
    }

    private static NodeData max(Set<NodeData> data) {
        return data.stream()
                .max(NodeData::compareTo)
                .orElseThrow(RuntimeException::new);
    }

    @Value
    static class Pair<T> {

        T first;
        T second;
    }

}
