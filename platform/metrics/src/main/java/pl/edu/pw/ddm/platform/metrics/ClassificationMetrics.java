package pl.edu.pw.ddm.platform.metrics;

import java.util.List;

import lombok.Getter;
import pl.edu.pw.ddm.platform.metrics.dto.IdLabel;
import pl.edu.pw.ddm.platform.metrics.dto.MetricsSummary;

public class ClassificationMetrics {

    private static final DdmAccuracy ACCURACY = new DdmAccuracy();
    private static final DdmPrecision PRECISION = new DdmPrecision();
    private static final DdmRecall RECALL = new DdmRecall();
    private static final DdmFMeasure F_MEASURE = new DdmFMeasure();

    private final SortedDataHolder dataHolder;

    @Getter
    private final MetricsSummary summary;

    public ClassificationMetrics(List<IdLabel> predictionResults, List<IdLabel> expectedResults) {
        this.dataHolder = new SortedDataHolder(predictionResults, expectedResults);
        this.summary = new MetricsSummary();
    }

    public double accuracy() {
        return summary.get(Metrics.ACCURACY, () -> ACCURACY.calculate(dataHolder, summary));
    }

    public double precision() {
        return summary.get(Metrics.PRECISION, () -> PRECISION.calculate(dataHolder, summary));
    }

    public double recall() {
        return summary.get(Metrics.RECALL, () -> RECALL.calculate(dataHolder, summary));
    }

    public double fMeasure() {
        return summary.get(Metrics.F_MEASURE, () -> F_MEASURE.calculate(dataHolder, summary));
    }

    @Override
    public String toString() {
        return summary.toString();
    }

}
