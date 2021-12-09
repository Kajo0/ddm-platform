package pl.edu.pw.ddm.platform.metrics;

import lombok.Getter;
import pl.edu.pw.ddm.platform.metrics.dto.IdLabel;
import pl.edu.pw.ddm.platform.metrics.dto.MetricsSummary;

import java.util.List;

public class ClusteringMetrics {

    private static final DdmAdjustedRandIndex ADJUSTED_RAND_INDEX = new DdmAdjustedRandIndex();
    private static final DdmAdjustedMutualInformation ADJUSTED_MUTUAL_INFORMATION = new DdmAdjustedMutualInformation();

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

    public double adjustedMutualInformation() {
        return summary.get(Metrics.ADJUSTED_MUTUAL_INFORMATION,
                () -> ADJUSTED_MUTUAL_INFORMATION.calculate(dataHolder, summary));
    }

    @Override
    public String toString() {
        return summary.toString();
    }

}
