package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.distance;

import java.util.List;

/**
 * Impl. of Cosinus measure between double[] points. Distance is calculated as
 * <code>
 * 1 - similarity
 * </code>
 */
public class CosinusFunction implements DistanceFunction<double[]> {

    @Override
    public double distance(double[] first, double[] second) {
        if (first.length != second.length) {
            throw new IllegalArgumentException();
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < first.length; i++) {
            dotProduct += first[i] * second[i];
            normA += Math.pow(first[i], 2);
            normB += Math.pow(second[i], 2);
        }

        return (1 - dotProduct / (Math.sqrt(normA) * Math.sqrt(normB)));
    }

    @Override
    public double[] meanMerge(List<double[]> list) {
        throw new RuntimeException("Not implemented");
    }

}
