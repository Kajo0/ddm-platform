package ddm.samples;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class MoreSophisticatedDenseExtractor {

    private final Collection<Data> data;
    private final DistanceFunction distanceFunction;
    private final double densePercent;
    private final double noDensityThreshold;

    DenseExtractor.DataIdWrapper extract() {
        Preconditions.checkState(data.size() > 0, "group contain 0 objects");
        double[] center = new NumericCenterFinder().findCenter(data, distanceFunction);

        List<DenseExtractor.Pair> sorted = data.stream()
                .map(p -> DenseExtractor.Pair.of(p, distanceFunction.distance(center, p.getNumericAttributes())))
                .sorted(DenseExtractor.Pair::compareTo)
                .collect(Collectors.toList());
        double max = Iterables.getLast(sorted).getDistance();
        double mean = sorted.stream()
                .map(DenseExtractor.Pair::getDistance)
                .reduce(Double::sum)
                .orElse(0d) / sorted.size();

        List<Data> dense = new ArrayList<>();
        List<Data> outliers = new ArrayList<>();

        int noDensityThresholdSize = (int) Math.max(sorted.size() * noDensityThreshold, 1);
        double sum = 0;
        double prevMean = 0;
        int i = 0;
        for (; i < sorted.size(); ++i) {
            DenseExtractor.Pair p = sorted.get(i);
            double tmpSum = sum + p.getDistance();
            double tmpMean = tmpSum / (i + 1);
            if ((p.getDistance() - prevMean) / mean < densePercent && i <= noDensityThresholdSize) {
                sum = tmpSum;
                prevMean = tmpMean;
                dense.add(p.getObject());
            } else {
                break;
            }
        }
        for (; i < sorted.size(); ++i) {
            DenseExtractor.Pair p = sorted.get(i);
            outliers.add(p.getObject());
        }

        return DenseExtractor.DataIdWrapper.of(dense, outliers);
    }

}
