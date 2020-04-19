package pl.edu.pw.ddm.platform.metrics;

import pl.edu.pw.ddm.platform.metrics.dto.MetricsSummary;
import pl.edu.pw.ddm.platform.metrics.util.ConversionUtils;
import smile.validation.ConfusionMatrix;

class DdmConfusionMatrix implements Metrics {

    @Override
    public Double calculate(SortedData sortedData, MetricsSummary summary) {
        int[] predictions = ConversionUtils.mapToInts(sortedData.predicationLabels());
        int[] labels = ConversionUtils.mapToInts(sortedData.realLabels());

        ConfusionMatrix confusion = ConfusionMatrix.of(labels, predictions);
        summary.set(Metrics.TRUE_POSITIVE, (double) confusion.matrix[0][0]);
        summary.set(Metrics.TRUE_NEGATIVE, (double) confusion.matrix[0][1]);
        summary.set(Metrics.FALSE_POSITIVE, (double) confusion.matrix[1][0]);
        summary.set(Metrics.FALSE_NEGATIVE, (double) confusion.matrix[1][1]);

        return null;
    }

}
