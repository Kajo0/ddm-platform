package ddm.samples;

import java.util.Collection;

import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;

class NumericCenterFinder {

    public double[] findCenter(Collection<Data> data, DistanceFunction distanceFunction) {
        double[] sum = data.stream()
                .map(Data::getNumericAttributes)
                .reduce(this::sum)
                .orElseThrow(IllegalStateException::new);
        return mean(sum, data.size());
    }

    private double[] sum(double[] first, double[] second) {
        double[] sum = new double[first.length];
        for (int i = 0; i < first.length; ++i) {
            sum[i] = first[i] + second[i];
        }
        return sum;
    }

    private double[] mean(double[] sum, int size) {
        for (int i = 0; i < sum.length; ++i) {
            sum[i] /= size;
        }
        return sum;
    }

}
