package pl.edu.pw.ddm.platform.metrics;

import pl.edu.pw.ddm.platform.metrics.dto.MetricsSummary;
import pl.edu.pw.ddm.platform.metrics.util.ConversionUtils;
import smile.validation.Precision;

class DdmPrecision implements Metrics {

    @Override
    public Double calculate(SortedData sortedData, MetricsSummary summary) {
        int[] predictions = ConversionUtils.mapToInts(sortedData.predicationLabels());
        int[] labels = ConversionUtils.mapToInts(sortedData.realLabels());

        // TODO improve performance by using and/or saving confusion matrix etc.
        return Precision.instance.measure(labels, predictions);
    }

}
