package pl.edu.pw.ddm.platform.algorithm.classification.naivebayes;

interface Distribution {

    double probability(double x, double mean, double stddev);

}
