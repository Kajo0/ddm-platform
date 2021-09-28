package pl.edu.pw.ddm.platform.strategies.conceptdrift;

import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Getter
class MultiBuckets {

    private final HashMap<Integer, Buckets> attrToValueToIds = new HashMap<>();

    void add(int column, String attributeValue, List<String> ids) {
        attrToValueToIds.computeIfAbsent(column, s -> new Buckets(column))
                .add(attributeValue, ids);
    }

    void remove(int column) {
        attrToValueToIds.remove(column);
    }

    void remove(List<SeparatedBucket> singularities) {
        var ids = singularities.stream()
                .map(SeparatedBucket::getValueToIds)
                .flatMap(Collection::stream)
                .map(IdValuesPair::getId)
                .collect(Collectors.toList());

        attrToValueToIds.values()
                .forEach(a -> ids.forEach(a::removeId));
    }

}
