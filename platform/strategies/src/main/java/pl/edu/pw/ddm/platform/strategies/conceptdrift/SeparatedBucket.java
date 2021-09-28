package pl.edu.pw.ddm.platform.strategies.conceptdrift;

import com.google.common.collect.Iterables;
import com.google.common.primitives.Doubles;
import lombok.Getter;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

@Getter
class SeparatedBucket {

    private final int columns;
    private final List<Integer> columnsExcluded;
    private final List<Pair<Integer, Boolean>> numericAttrs;
    private final List<IdValuesPair> valueToIds = new ArrayList<>(); // FIXME wrong name? cause id to values?
    private final List<Set<String>> valuesInside = new ArrayList<>();

    SeparatedBucket(int columns, List<Integer> columnsExcluded, List<Pair<Integer, Boolean>> numericAttrs) {
        this.columns = columns;
        this.columnsExcluded = columnsExcluded;
        this.numericAttrs = numericAttrs;

        for (int i = 0; i < columns; ++i) {
            if (numericAttrs.get(i).getValue()) {
                valuesInside.add(new TreeSet<>(Comparator.comparingDouble(Doubles::tryParse)));
            } else {
                valuesInside.add(new TreeSet<>(Comparator.comparing(String::toString)));
            }
        }
    }

    boolean hasValues(IdValuesPair pair) {
        int j = 0;
        for (int i = 0; i < columns + columnsExcluded.size(); ++i) {
            if (!columnsExcluded.contains(i)) {
                if (valuesInside.get(j++).contains(pair.getValues()[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    void add(IdValuesPair pair) {
        valueToIds.add(pair);
        int j = 0;
        for (int i = 0; i < columns + columnsExcluded.size(); ++i) {
            if (!columnsExcluded.contains(i)) {
                valuesInside.get(j++)
                        .add(pair.getValues()[i]);
            }
        }
    }

    boolean isSeparated() {
        var allNominal = numericAttrs.stream()
                .noneMatch(Pair::getValue);
        return allNominal &&
                valuesInside.stream()
                        .allMatch(v -> v.size() == 1);
    }

    int idsInside() {
        return valueToIds.size();
    }

    double sortValue() {
        var a = Iterables.getFirst(valuesInside, Collections.<String>emptySet());
        var b = Iterables.getFirst(a, String.valueOf(Double.MAX_VALUE));
        return Optional.ofNullable(b)
                .map(Doubles::tryParse)
                .orElse((double) b.hashCode());
    }

    @Override
    public String toString() {
        return valueToIds.size() + " ids of " + valuesInside;
    }
}
