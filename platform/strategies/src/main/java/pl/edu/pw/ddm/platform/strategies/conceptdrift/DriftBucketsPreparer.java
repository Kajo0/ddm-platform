package pl.edu.pw.ddm.platform.strategies.conceptdrift;

import com.google.common.primitives.Doubles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
class DriftBucketsPreparer {

    private final int attrCount;
    private final int drifts;
    private final Map<String, IdValuesPair> oneLabelData;
    private final boolean[] numericAttrs;

    private final BucketedIds bucketedIds = new BucketedIds();
    private boolean processed;

    BucketedIds prepare() {
        if (processed) {
            return bucketedIds;
        } else {
            processed = true;
        }

        // initial grouping by value=ids per each attribute
        var buckets = groupAttributes();
        var columnsNumericAttrs = new ArrayList<Pair<Integer, Boolean>>();
        for (int i = 0; i < numericAttrs.length; ++i) {
            columnsNumericAttrs.add(new Pair<>(i, numericAttrs[i]));
        }

        var columnsExcluded = new ArrayList<Integer>();
        var separatedColumnToBucketsMap = new HashMap<Integer, List<SeparatedBucket>>();
        var singularitiesMap = new HashMap<Integer, List<SeparatedBucket>>();
        while (!buckets.getAttrToValueToIds().isEmpty()) {
            var finder = new SeparatedFinder(oneLabelData,
                    buckets,
                    buckets.getAttrToValueToIds().size(),
                    columnsNumericAttrs,
                    columnsExcluded);
            // start from checking the lowest attribute entropy (the lowest amount of different values)
            var column = finder.findLowestEntropyColumn();
            log.info("Considered {} column as the one with lowest entropy", column);
            // group data by chosen attribute values and separate individuals (where attributes separates from other data samples attrs.)
            var separatedBuckets = finder.find();
            separatedBuckets.sort(Comparator.comparing(SeparatedBucket::sortValue));
            separatedColumnToBucketsMap.put(column, separatedBuckets);

            // remove already processed dimension (attribute) from the next pass
            buckets.remove(column);
            columnsNumericAttrs.removeIf(c -> c.getKey().equals(column));
            // remove individuals as they are already 'drifted' from other samples,
            //  so if e.g. an individual has ID=5 then remove such id from every bucket that may occur via its' attribute
            var singularities = separatedBuckets.stream()
                    .filter(SeparatedBucket::isSeparated)
                    .collect(Collectors.toList());
            singularitiesMap.put(column, singularities);

            // remove individuals from buckets that will be processed in the next pass if next pass will be necessary
            buckets.remove(singularities);
            // exclude already analyzed attribute for the next pass
            columnsExcluded.add(column);

            // check whenever the declared drift level is available or no attributes left to analyze
            var enable = checkIfPossibleEndInThisStep(separatedBuckets, singularitiesMap);
            if (enable || buckets.getAttrToValueToIds().isEmpty()) {
                //  and process final drift separation if finished
                process(separatedColumnToBucketsMap, singularitiesMap, columnsExcluded);
                break;
            }
        }

        if (bucketedIds.driftToIds.size() < 2) {
            throw new IllegalStateException("Not able to drift such data");
        }
        return bucketedIds;
    }

    private void process(HashMap<Integer, List<SeparatedBucket>> separatedColumnToBucketsMap,
                         HashMap<Integer, List<SeparatedBucket>> singularitiesMap,
                         ArrayList<Integer> columnsExcluded) {
        var independentSingularities = new ArrayList<SeparatedBucket>();
        var independentSingAmount = 0;
        var columnsExcludedButLast = columnsExcluded.stream()
                .limit(columnsExcluded.size() - 1)
                .collect(Collectors.toList());
        for (int col : columnsExcludedButLast) {
            var val = singularitiesMap.get(col);
            independentSingularities.addAll(val);
            independentSingAmount += val.stream()
                    .mapToInt(SeparatedBucket::idsInside)
                    .sum();
        }

        var col = columnsExcluded.get(columnsExcluded.size() - 1);
        log.info("Drifting by {} column while checked {}", col, columnsExcluded);
        var toDivide = separatedColumnToBucketsMap.get(col);
        var result = new DriftDivider(toDivide, drifts, independentSingAmount)
                .divide();

        // TODO maybe add singularities in the buckets where the amount of data is the lowest?
        int i = 0;
        for (var sing : independentSingularities) {
            // if all numeric, find the closest group, random otherwise
            var allNumerical = !sing.getNumericAttrs().isEmpty() && sing.getNumericAttrs()
                    .stream()
                    .allMatch(Pair::getValue);
            if (allNumerical) {
                findClosest(sing, result)
                        .add(sing);
            } else {
                result.get(i++)
                        .add(sing);
                i %= result.size();
            }
        }

        // TODO maybe here divide 2 drifts into 4 (equal 2 + 2) when 4 workers but what if not equal?

        result.forEach(bucketedIds::putAll);
    }

    private List<SeparatedBucket> findClosest(SeparatedBucket sing, HashMap<Integer, List<SeparatedBucket>> result) {
        var sId = sing.getValueToIds().get(0).getId();
        var sData = oneLabelData.get(sId);
        var numData = Arrays.stream(sData.getValues()).mapToDouble(Doubles::tryParse).toArray();

        // FIXME adjust it a bit using some more sophisticated method than comparing to the middle value
        var middles = result.values()
                .stream()
                .map(a -> a.get(a.size() / 2))
                .map(a -> a.getValueToIds().get(0).getId())
                .map(oneLabelData::get)
                .map(a -> Arrays.stream(a.getValues()).mapToDouble(Doubles::tryParse).toArray())
                .map(a -> new EuclideanDistance().compute(numData, a))
                .collect(Collectors.toList());

        int minIdx = 0;
        double minVal = Double.MAX_VALUE;
        for (int i = 0; i < middles.size(); ++i) {
            if (middles.get(i) < minVal) {
                minVal = middles.get(i);
                minIdx = i;
            }
        }

        return result.get(minIdx);
    }

    private boolean checkIfPossibleEndInThisStep(List<SeparatedBucket> separatedBuckets,
                                                 HashMap<Integer, List<SeparatedBucket>> singularitiesMap) {
        var nonSingularities = separatedBuckets.stream()
                .filter(Predicate.not(SeparatedBucket::isSeparated))
                .collect(Collectors.toList());
        // TODO somehow use previous independent singularities.size() in equation
//        var singularities = singularitiesMap.values()
//                .stream()
//                .flatMap(Collection::stream)
//                .collect(Collectors.toList());
        // FIXME make it more sophisticated to avoid situation eg. 13, 1, 1 for drift equal 3 cause it will be uneven
        var countThreshold = (oneLabelData.size() / drifts) * 0.2;
        return nonSingularities.size() >= drifts
                && (nonSingularities.size() >= countThreshold
                || nonSingularities.stream()
                .mapToInt(SeparatedBucket::idsInside)
                .allMatch(count -> count > countThreshold));
    }

    private MultiBuckets groupAttributes() {
        MultiBuckets initialBuckets = new MultiBuckets();
        for (int i = 0; i < attrCount; ++i) {
            int finalI = i;
            oneLabelData.values()
                    .stream()
                    .collect(Collectors.groupingBy(p -> p.getValues()[finalI]))
                    .forEach((val, pair) -> {
                        var ids = pair.stream()
                                .map(IdValuesPair::getId)
                                .collect(Collectors.toList());
                        initialBuckets.add(finalI, val, ids);
                    });
        }
        return initialBuckets;
    }

    static class BucketedIds {

        private final HashMap<Integer, List<String>> driftToIds = new HashMap<>();
        // TODO remove for non-tests as it is used only for charts
        private final HashMap<Integer, List<IdValuesPair>> driftToPairs = new HashMap<>();

        int bucketNumber(String id) {
            return driftToIds.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().contains(id))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Not found id " + id + " in buckets."));
        }

        void putAll(Integer num, List<SeparatedBucket> separatedBuckets) {
            var ids = separatedBuckets.stream()
                    .map(SeparatedBucket::getValueToIds)
                    .flatMap(Collection::stream)
                    .map(IdValuesPair::getId)
                    .collect(Collectors.toList());
            driftToIds.put(num, ids);

            // TODO remove for non-tests
            var pairs = separatedBuckets.stream()
                    .map(SeparatedBucket::getValueToIds)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            driftToPairs.put(num, pairs);
        }
    }

}
