package ddm.samples;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;

@RequiredArgsConstructor
class DenseExtractor {

    private final Collection<Data> data;
    private final DistanceFunction distanceFunction;
    private final double densePercent;

    DataIdWrapper extract() {
        Preconditions.checkState(data.size() > 0, "group contain 0 objects");
        double[] center = new NumericCenterFinder().findCenter(data, distanceFunction);

        List<Pair> sorted = data.stream()
                .map(p -> Pair.of(p, distanceFunction.distance(center, p.getNumericAttributes())))
                .sorted(Pair::compareTo)
                .collect(Collectors.toList());
        double max = Iterables.getLast(sorted).distance;
        double threshold = densePercent * max;

        Map<Boolean, List<Pair>> map = sorted.stream()
                .collect(Collectors.groupingBy(a -> a.distance <= threshold));

        List<Data> dense = map.getOrDefault(true, Collections.emptyList())
                .stream()
                .map(Pair::getObject)
                .collect(Collectors.toList());
        List<Data> outliers = map.getOrDefault(false, Collections.emptyList())
                .stream()
                .map(Pair::getObject)
                .collect(Collectors.toList());
        return DataIdWrapper.of(dense, outliers);
    }

    @Value(staticConstructor = "of")
    static class DataIdWrapper {

        List<Data> dense;
        List<Data> outliers;
    }

    @Value(staticConstructor = "of")
    static class Pair implements Comparable<Pair> {

        Data object;
        double distance;

        @Override
        public int compareTo(Pair o) {
            return Double.compare(distance, o.distance);
        }
    }

}
