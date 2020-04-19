package pl.edu.pw.ddm.platform.metrics;

import java.util.List;

import lombok.Getter;
import pl.edu.pw.ddm.platform.metrics.dto.IdLabel;
import pl.edu.pw.ddm.platform.metrics.dto.MetricsSummary;

public class ClusteringMetrics {

    private static final DdmAdjustedRandIndex ADJUSTED_RAND_INDEX = new DdmAdjustedRandIndex();

    private final SortedDataHolder dataHolder;

    @Getter
    private final MetricsSummary summary;

    public ClusteringMetrics(List<IdLabel> predictionResults, List<IdLabel> expectedResults) {
        this.dataHolder = new SortedDataHolder(predictionResults, expectedResults);
        this.summary = new MetricsSummary();
    }

    public double adjustedRandIndex() {
        return summary.get(Metrics.ADJUSTED_RAND_INDEX, () -> ADJUSTED_RAND_INDEX.calculate(dataHolder, summary));
    }

    @Override
    public String toString() {
        return summary.toString();
    }

}
