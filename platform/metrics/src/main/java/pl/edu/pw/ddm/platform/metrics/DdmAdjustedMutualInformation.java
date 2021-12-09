package pl.edu.pw.ddm.platform.metrics;

import pl.edu.pw.ddm.platform.metrics.dto.MetricsSummary;
import pl.edu.pw.ddm.platform.metrics.util.ConversionUtils;
import smile.validation.AdjustedMutualInformation;

class DdmAdjustedMutualInformation implements Metrics {

    @Override
    public Double calculate(SortedData sortedData, MetricsSummary summary) {
        int[][] ints = ConversionUtils.mapToInts(sortedData.realLabels(), sortedData.predicationLabels());

        // TODO add other normalizer options like sqrt, min, max
        return AdjustedMutualInformation.sum(ints[0], ints[1]);
    }

}
