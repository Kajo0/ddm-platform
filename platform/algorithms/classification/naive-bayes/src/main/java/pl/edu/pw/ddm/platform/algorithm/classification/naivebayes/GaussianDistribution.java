package pl.edu.pw.ddm.platform.algorithm.classification.naivebayes;

class GaussianDistribution implements Distribution {

    private static final double P = 1d / (Math.sqrt(2.0 * Math.PI));

    @Override
    public double probability(double x, double mean, double stddev) {
        // FIXME to avoid '0' stddev in formula
        if (stddev == 0) {
            return 0;
        }
        double exp = Math.exp(-0.5 * Math.pow((x - mean) / stddev, 2));
        return (1 / stddev) * P * exp;
    }

}
