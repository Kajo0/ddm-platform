package pl.edu.pw.ddm.platform.metrics;

import pl.edu.pw.ddm.platform.metrics.dto.MetricsSummary;
import pl.edu.pw.ddm.platform.metrics.util.ConversionUtils;
import smile.validation.ConfusionMatrix;

class DdmFowlkesMallowsIndex implements Metrics {

    @Override
    public Double calculate(SortedData sortedData, MetricsSummary summary) {
        int[][] ints = ConversionUtils.mapToInts(sortedData.realLabels(), sortedData.predicationLabels());
        int[][] matrix = ConfusionMatrix.of(ints[0], ints[1]).matrix;

        int tptn = 0;
        for (int i = 0; i < matrix.length; ++i) {
            tptn += matrix[i][i];
        }

        int fp = 0;
        for (int j = 0; j < matrix.length; ++j) {
            for (int i = j + 1; i < matrix[0].length; ++i) {
                fp += matrix[j][i];
            }
        }

        int fn = 0;
        for (int j = 0; j < matrix.length; ++j) {
            for (int i = 0; i < j; ++i) {
                fn += matrix[j][i];
            }
        }

        return tptn / Math.sqrt((tptn + fp) * (tptn + fn));
    }

}
