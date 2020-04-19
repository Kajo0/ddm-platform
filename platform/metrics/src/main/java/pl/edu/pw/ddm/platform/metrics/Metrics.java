package pl.edu.pw.ddm.platform.metrics;

import pl.edu.pw.ddm.platform.metrics.dto.MetricsSummary;

public interface Metrics {

    String ADJUSTED_RAND_INDEX = "ARI";

    String TRUE_POSITIVE = "TP";
    String TRUE_NEGATIVE = "TN";
    String FALSE_POSITIVE = "FP";
    String FALSE_NEGATIVE = "FN";

    String ACCURACY = "accuracy";
    String PRECISION = "precision";
    String RECALL = "recall";
    String F_MEASURE = "f-measure";

    Double calculate(SortedData sortedData, MetricsSummary summary);

}
