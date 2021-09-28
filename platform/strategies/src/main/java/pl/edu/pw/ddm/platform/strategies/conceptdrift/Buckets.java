package pl.edu.pw.ddm.platform.strategies.conceptdrift;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
class Buckets {

    private final Integer column;
    private final Map<String, List<String>> valueToIds = new HashMap<>();

    void add(String attributeValue, List<String> ids) {
        valueToIds.put(attributeValue, ids);
    }

    void removeId(String id) {
        valueToIds.values()
                .forEach(a -> a.remove(id));

        var empty = valueToIds.entrySet()
                .stream()
                .filter(a -> a.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        empty.forEach(valueToIds::remove);
    }

}
