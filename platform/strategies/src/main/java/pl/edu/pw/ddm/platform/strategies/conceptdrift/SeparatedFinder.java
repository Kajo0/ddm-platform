package pl.edu.pw.ddm.platform.strategies.conceptdrift;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class SeparatedFinder {

    private final Map<String, IdValuesPair> oneLabelData;
    private final MultiBuckets buckets;
    private final int columns;
    private final List<Pair<Integer, Boolean>> numericAttrs;
    private final List<Integer> columnsExcluded;

    private final List<SeparatedBucket> separatedBuckets = new ArrayList<>();

    private Buckets selected;

    List<SeparatedBucket> find() {
        Preconditions.checkNotNull(selected, "Find lowest entropy column first");

        selected.getValueToIds()
                .forEach((value, ids) -> {
                    ids.forEach(id -> {
                        var pair = oneLabelData.get(id);
                        var bucket = getSeparatedBucket(pair);
                        bucket.add(pair);
                    });
                });

        return separatedBuckets;
    }

    private SeparatedBucket getSeparatedBucket(IdValuesPair pair) {
        var bucket = separatedBuckets.stream()
                .filter(b -> b.hasValues(pair))
                .findAny()
                .orElse(null);
        if (bucket == null) {
            bucket = new SeparatedBucket(columns, columnsExcluded, numericAttrs);
            separatedBuckets.add(bucket);
        }
        return bucket;
    }

    int findLowestEntropyColumn() {
        Preconditions.checkState(!buckets.getAttrToValueToIds().isEmpty(), "No buckets to find in.");

        var other = buckets.getAttrToValueToIds()
                .values()
                .stream()
                .sorted(Comparator.comparingInt(a -> a.getValueToIds().size()))
                .collect(Collectors.toList());
        selected = other.remove(0);

        return selected.getColumn();
    }

}
