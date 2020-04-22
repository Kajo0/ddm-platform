package pl.edu.pw.ddm.platform.metrics;

import java.util.stream.DoubleStream;

import pl.edu.pw.ddm.platform.metrics.dto.MetricsSummary;
import pl.edu.pw.ddm.platform.metrics.util.ConversionUtils;
import smile.validation.ConfusionMatrix;

class DdmPrecision implements Metrics {

    @Override
    public Double calculate(SortedData sortedData, MetricsSummary summary) {
        int[][] ints = ConversionUtils.mapToInts(sortedData.realLabels(), sortedData.predicationLabels());

        int[][] matrix = ConfusionMatrix.of(ints[0], ints[1]).matrix;

        double[] precisions = new double[matrix.length];
        for (int i = 0; i < matrix.length; ++i) {
            precisions[i] = matrix[i][i];
            int rowsum = 0;
            for (int j = 0; j < matrix.length; ++j) {
                rowsum += matrix[j][i];
            }

            if (rowsum == 0) {
                precisions[i] = 0;
            } else {
                precisions[i] /= rowsum;
            }
        }

        return DoubleStream.of(precisions)
                .average()
                .orElse(0);
    }

}
