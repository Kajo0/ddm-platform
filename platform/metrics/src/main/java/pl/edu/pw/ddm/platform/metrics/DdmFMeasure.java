package pl.edu.pw.ddm.platform.metrics;

import pl.edu.pw.ddm.platform.metrics.dto.MetricsSummary;

class DdmFMeasure implements Metrics {

    @Override
    public Double calculate(SortedData sortedData, MetricsSummary summary) {
        double precision = summary.get(Metrics.PRECISION, () -> new DdmRecall().calculate(sortedData, summary));
        double recall = summary.get(Metrics.RECALL, () -> new DdmRecall().calculate(sortedData, summary));

        return 2 * precision * recall / (precision + recall);
    }

}
