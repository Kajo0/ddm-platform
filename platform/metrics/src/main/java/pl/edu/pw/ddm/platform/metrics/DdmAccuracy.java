package pl.edu.pw.ddm.platform.metrics;

import pl.edu.pw.ddm.platform.metrics.dto.MetricsSummary;
import pl.edu.pw.ddm.platform.metrics.util.ConversionUtils;
import smile.validation.Accuracy;

class DdmAccuracy implements Metrics {

    @Override
    public Double calculate(SortedData sortedData, MetricsSummary summary) {
        int[][] ints = ConversionUtils.mapToInts(sortedData.realLabels(), sortedData.predicationLabels());

        return Accuracy.instance.measure(ints[0], ints[1]);
    }

}
