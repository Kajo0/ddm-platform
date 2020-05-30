package pl.edu.pw.ddm.platform.algorithms.clustering.lct;

import lombok.experimental.UtilityClass;
import pl.edu.pw.ddm.platform.distfunc.EuclideanDistance;

@UtilityClass
class ClusterMerger {

    private final EuclideanDistance EUCLIDEAN_DISTANCE = new EuclideanDistance();

    boolean shouldMerge(LModel.LocalCluster first, LModel.LocalCluster second) {
        double maxVarianceThreshold = 2 * Math.max(first.getVariance(), second.getVariance());
        return var(first, second) < maxVarianceThreshold;
    }

    LModel.LocalCluster merge(LModel.LocalCluster first, LModel.LocalCluster second) {
        int newSize = nnew(first, second);
        double[] newCenter = cnew(first, second);
        double newVariance = var(first, second);

        return LModel.LocalCluster.of(newCenter, newSize, newVariance);
    }

    double var(LModel.LocalCluster first, LModel.LocalCluster second) {
        return first.getVariance() + second.getVariance() + inc(first, second);
    }

    private int nnew(LModel.LocalCluster first, LModel.LocalCluster second) {
        return first.getSize() + second.getSize();
    }

    private double[] cnew(LModel.LocalCluster first, LModel.LocalCluster second) {
        int nnew = nnew(first, second);
        double ni = (double) first.getSize() / nnew;
        double nj = (double) second.getSize() / nnew;

        double[] firstCentroid = first.getCentroid();
        double[] secondCentroid = second.getCentroid();
        double[] result = new double[firstCentroid.length];
        for (int i = 0; i < firstCentroid.length; ++i) {
            result[i] = ni * firstCentroid[i] + nj * secondCentroid[i];
        }

        return result;
    }

    private double inc(LModel.LocalCluster first, LModel.LocalCluster second) {
        return (double) first.getSize() * second.getSize()
                / (first.getSize() + second.getSize())
                * EUCLIDEAN_DISTANCE.distance(first.getCentroid(), second.getCentroid());
    }

}
