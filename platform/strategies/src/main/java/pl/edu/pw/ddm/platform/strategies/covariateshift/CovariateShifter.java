package pl.edu.pw.ddm.platform.strategies.covariateshift;

import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class CovariateShifter {

    public static final double OBJECTIVE_THRESHOLD = 0.005;
    public static final int MAX_ITERATIONS = 1000;

    private final List<IdValuePair> idToAttrSorted;
    private final long samplesCount;
    private final double targetShift;
    private final int targetSplits;
    private final Random rand;
    private final Integer splitMethodOrdinal;

    private final Map<Integer, List<IdValuePair>> splits = new HashMap<>();

    private double min;
    private double max;
    private double lastCalc = 1d;
    private boolean processed;

    public Map<Integer, List<IdValuePair>> shift() {
        if (processed) {
            return splits;
        } else {
            processed = true;
        }

        SplitMethods method;
        if (splitMethodOrdinal == null) {
            method = SplitMethods.EQUAL_SPLIT_RANDOM_WITH_ROLLBACK;
        } else {
            method = SplitMethods.values()[splitMethodOrdinal];
        }
        findMinMax();

        switch (method) {
            case EQUAL_SPLIT_RANDOM_WITH_ROLLBACK:
            case EQUAL_SPLIT_RANDOM:
            case EQUAL_SPLIT_BORDER:
                splitEqual();
                break;
            case RANDOM_SPLIT_RANDOM_WITH_ROLLBACK:
            case RANDOM_SPLIT_RANDOM:
            case RANDOM_SPLIT_BORDER:
                splitRandom();
                break;

            default:
                throw new IllegalArgumentException("Unknown split method: " + splitMethodOrdinal);
        }

        int i = 0;
        do {
            switch (method) {
                case EQUAL_SPLIT_RANDOM_WITH_ROLLBACK:
                case RANDOM_SPLIT_RANDOM_WITH_ROLLBACK:
                    moveRandom(true);
                    break;
                case EQUAL_SPLIT_RANDOM:
                case RANDOM_SPLIT_RANDOM:
                    moveRandom(false);
                    break;
                case EQUAL_SPLIT_BORDER:
                case RANDOM_SPLIT_BORDER:
                    moveBorderElements(i);
                    break;
            }

            lastCalc = calcObjective(splits);
        } while (lastCalc > OBJECTIVE_THRESHOLD && i++ < MAX_ITERATIONS);

        return splits;
    }

    private void findMinMax() {
        min = idToAttrSorted.stream()
                .map(IdValuePair::getValue)
                .mapToDouble(Double::valueOf)
                .min()
                .orElse(0);
        max = idToAttrSorted.stream()
                .map(IdValuePair::getValue)
                .mapToDouble(Double::valueOf)
                .max()
                .orElse(0);
    }

    private void moveBorderElements(int lastNum) {
        var from = lastNum % targetSplits;
        var to = from + 1;
        if (from + 1 == targetSplits) {
            from = targetSplits - 2;
            to = targetSplits - 1;
        }

        var whichFrom = Math.abs(splits.get(from).size() - lastNum / targetSplits - 1) % splits.get(from).size();
        var whichTo = lastNum / targetSplits % splits.get(to).size();

        var fromVal = splits.get(from).remove(whichFrom);
        var toVal = splits.get(to).remove(whichTo);

        splits.get(from).add(toVal);
        splits.get(to).add(fromVal);
    }

    private void moveRandom(boolean withRollback) {
        var breakAfter = 100;
        int from;
        do {
            from = rand.nextInt(targetSplits);
            if (--breakAfter < 0) {
                throw new IllegalStateException("Cannot split data using random strategy.");
            }
        } while (splits.get(from).size() <= 1);

        breakAfter = 100;
        var to = from;
        do {
            to = rand.nextInt(targetSplits);
            if (--breakAfter < 0) {
                throw new IllegalStateException("Cannot split data using random strategy.");
            }
        } while (from == to || splits.get(to).size() <= 1);

        var whichFrom = rand.nextInt(splits.get(from).size());
        var whichTo = rand.nextInt(splits.get(to).size());

        var fromVal = splits.get(from).remove(whichFrom);
        var toVal = splits.get(to).remove(whichTo);

        if (withRollback) {
            var copySplits = new HashMap<Integer, List<IdValuePair>>();
            splits.forEach((k, v) -> {
                var copy = new ArrayList<>(v);
                copySplits.put(k, copy);
            });
            copySplits.get(from).add(toVal);
            copySplits.get(to).add(fromVal);

            var eps = calcObjective(copySplits);
            if (lastCalc < eps) {
                // do not move when worse func result - revert remove
                splits.get(from).add(whichFrom, fromVal);
                splits.get(to).add(whichTo, toVal);
            } else {
                // apply movement
                splits.get(from).add(toVal);
                splits.get(to).add(fromVal);
            }
        } else {
            splits.get(from).add(toVal);
            splits.get(to).add(fromVal);
        }
    }

    /**
     * Objective function: {[(Mn - Mn-1) + (Mn-2 - Mn-3) + (..) + (M2 - M1)]/max-min} / (shifts) == shift
     */
    private double calcObjective(Map<Integer, List<IdValuePair>> splits) {
        // calc distribution stats
        var ms = splits.values()
                .stream()
                .map(v -> {
                    var st = new SummaryStatistics();
                    v.stream()
                            .map(IdValuePair::getValue)
                            .map(Double::valueOf)
                            .forEach(st::addValue);
                    return st;
                })
                .map(SummaryStatistics::getMean)
                .collect(Collectors.toList());

        var count = ms.size();
        var numerator = 0d;
        for (int i = count - 1; i > 0; --i) {
            numerator += ms.get(i) - ms.get(i - 1);
        }
        var result = numerator / (max - min);
        result /= count - 1;

        return Math.abs(result - targetShift);
    }

    private void splitEqual() {
        var count = (int) Math.ceil((double) samplesCount / targetSplits);
        int i = 0;
        var j = 1;

        for (var entry : idToAttrSorted) {
            var list = splits.computeIfAbsent(i, k -> new ArrayList<>());
            list.add(entry);

            if (j % count == 0) {
                ++i;
            }
            ++j;
        }
    }

    private void splitRandom() {
        for (var entry : idToAttrSorted) {
            int i = rand.nextInt(targetSplits);
            var list = splits.computeIfAbsent(i, k -> new ArrayList<>());
            list.add(entry);
        }
    }

    @RequiredArgsConstructor
    enum SplitMethods {
        EQUAL_SPLIT_RANDOM_WITH_ROLLBACK("erwr"),
        EQUAL_SPLIT_RANDOM("er"),
        EQUAL_SPLIT_BORDER("eb"),
        RANDOM_SPLIT_RANDOM_WITH_ROLLBACK("rrwr"),
        RANDOM_SPLIT_RANDOM("rr"),
        RANDOM_SPLIT_BORDER("rb");

        private final String code;
    }

}
