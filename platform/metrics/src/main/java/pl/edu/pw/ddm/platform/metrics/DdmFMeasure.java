package pl.edu.pw.ddm.platform.metrics;

import pl.edu.pw.ddm.platform.metrics.dto.MetricsSummary;
import pl.edu.pw.ddm.platform.metrics.util.ConversionUtils;
import smile.validation.FMeasure;

class DdmFMeasure implements Metrics {

    @Override
    public Double calculate(SortedData sortedData, MetricsSummary summary) {
        int[] predictions = ConversionUtils.mapToInts(sortedData.predicationLabels());
        int[] labels = ConversionUtils.mapToInts(sortedData.realLabels());

        return FMeasure.instance.measure(labels, predictions);
    }

}
