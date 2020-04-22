package pl.edu.pw.ddm.platform.metrics;

import java.util.stream.DoubleStream;

import pl.edu.pw.ddm.platform.metrics.dto.MetricsSummary;
import pl.edu.pw.ddm.platform.metrics.util.ConversionUtils;
import smile.validation.ConfusionMatrix;

class DdmRecall implements Metrics {

    @Override
    public Double calculate(SortedData sortedData, MetricsSummary summary) {
        int[][] ints = ConversionUtils.mapToInts(sortedData.realLabels(), sortedData.predicationLabels());

        int[][] matrix = ConfusionMatrix.of(ints[0], ints[1]).matrix;

        double[] recalls = new double[matrix.length];
        for (int i = 0; i < matrix.length; ++i) {
            recalls[i] = matrix[i][i];
            int rowsum = 0;
            for (int j = 0; j < matrix.length; ++j) {
                rowsum += matrix[i][j];
            }

            if (rowsum == 0) {
                recalls[i] = 0;
            } else {
                recalls[i] /= rowsum;
            }
        }

        return DoubleStream.of(recalls)
                .average()
                .orElse(0);
    }

}
