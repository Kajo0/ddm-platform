package pl.edu.pw.ddm.platform.strategies.mostof;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
class NodeToLabelScattering {

    private final NodeLabelMap full = new NodeLabelMap();
    private final NodeLabelMap empty = new NodeLabelMap();
    private final NodeLabelMap additional = new NodeLabelMap();

    Set<Integer> labels() {
        return Stream.of(full, empty, additional)
                .map(a -> a.map.values())
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    Set<Integer> nodes() {
        return Stream.of(full, empty, additional)
                .map(a -> a.map.keySet())
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @ToString
    static class NodeLabelMap {

        private final Map<Integer, Set<Integer>> map = new HashMap<>();

        boolean add(Integer node, Integer label) {
            return map.computeIfAbsent(node, k -> new HashSet<>())
                    .add(label);
        }

        boolean has(Integer node, Integer label) {
            return map.getOrDefault(node, Collections.emptySet())
                    .contains(label);
        }

        int size(Integer node) {
            return map.getOrDefault(node, Collections.emptySet())
                    .size();
        }

        Set<Integer> labels(Integer node) {
            return map.get(node);
        }

        Set<Integer> nodes() {
            return map.keySet();
        }

        int countLabelInNodes(Integer label) {
            return (int) map.values()
                    .stream()
                    .filter(set -> set.contains(label))
                    .count();
        }
    }

}