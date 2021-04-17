package pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.utils;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;

@Getter
@Setter
public class Cluster implements Serializable {

    private transient boolean debug;
    private transient DistanceFunction distanceFunction;

    private boolean supportCluster; // TODO remove
    private Centroid centroid;
    private List<LabeledObservation> elements;
    private DensityStats primaryDensityStats = new DensityStats();

    // TODO make use of the function after removing weka impl.
    public Cluster(DistanceFunction distanceFunction, boolean debug) {
        this.distanceFunction = distanceFunction;
        this.debug = debug;
    }

    public void setCentroid(Centroid centroid) {
        this.centroid = centroid;
        primaryDensityStats.reset();
    }

    public void findCentroid() {
        double[] mean = new double[elements.get(0).getFeatures().length];
        for (LabeledObservation e : elements) {
            for (int i = 0; i < mean.length; ++i) {
                mean[i] += e.getFeatures()[i];
            }
        }
        for (int i = 0; i < mean.length; ++i) {
            mean[i] /= elements.size();
        }

        setCentroid(new Centroid(mean));
    }

    public LabeledObservation squashToCentroid() {
        primaryDensityStats.elements = elements.size();
        LabeledObservation any = elements.get(0);
        elements.clear();
        LabeledObservation squashed = new LabeledObservation(-1, centroid.getFeatures(), any.getTarget());
        elements.add(squashed);
        if (debug) {
            System.out.println("  [[FUTURE LOG]] Cluster with " + primaryDensityStats.elements + " elements squashed into 1");
        }
        return squashed;
    }

    public LabeledObservation squashToMedian() {
        primaryDensityStats.elements = elements.size();
        LabeledObservation median = elements.stream()
                .sorted(Comparator.comparingDouble(d -> distanceFunction.distance(centroid.getFeatures(), d.getFeatures())))
                .skip(primaryDensityStats.elements / 2)
                .findFirst()
                .get();
        elements.clear();
        elements.add(median);
        if (debug) {
            System.out.println("  [[FUTURE LOG]] Cluster with " + primaryDensityStats.elements + " elements medianed into 1");
        }
        return median;
    }

    public List<LabeledObservation> random(double percent, Long seed) {
        Random rand = Optional.ofNullable(seed)
                .map(Random::new)
                .orElseGet(Random::new);
        Collections.shuffle(elements, rand);

        if (percent < 0) {
            calculateMetrics();
            percent = clusterDensityPercent();
            if (debug) {
                System.out.println("  [[FUTURE LOG]] Random precalculated into " + percent + " value");
            }
        }
        if (percent == 0) {
            elements.clear();
        } else {
            long limit = Math.max(1, (long) Math.ceil(elements.size() * percent));
            if (percent == 0) {
                limit = 0;
            }

            primaryDensityStats.elements = elements.size();
            elements = elements.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
            if (debug) {
                System.out.println("  [[FUTURE LOG]] Cluster with " + primaryDensityStats.elements + " reduced randomly to " + elements.size() + " elements");
            }
        }

        return elements;
    }

    private double clusterDensityPercent() {
        return (primaryDensityStats.sum / (primaryDensityStats.elements * primaryDensityStats.max)) * (
                primaryDensityStats.stddev / primaryDensityStats.mean);
    }

    public LabeledObservation calculateMetrics() {
        primaryDensityStats.elements = elements.size();

        primaryDensityStats.max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        double S = 0;
        double SS = 0;
        double[] fS = new double[primaryDensityStats.fMax.length];
        double[] fSS = new double[primaryDensityStats.fMax.length];
        LabeledObservation closest = null;

        for (LabeledObservation p : elements) {
            double distance = distanceFunction.distance(centroid.getFeatures(), p.getFeatures());
            S += distance;
            SS += distance * distance;
            if (primaryDensityStats.max < distance) {
                primaryDensityStats.max = distance;
            }
            if (min > distance) {
                min = distance;
                closest = p;
            }

            for (int i =0 ; i < fS.length; ++i) {
                double value = p.getFeatures()[i];

                fS[i] += value;
                fSS[i] += value * value;
                if (primaryDensityStats.fMax[i] < value) {
                    primaryDensityStats.fMax[i] = value;
                }
                if (primaryDensityStats.fMin[i] > value) {
                    primaryDensityStats.fMin[i] = value;
                }
            }
        }

        primaryDensityStats.sum = S;
        primaryDensityStats.mean = S / primaryDensityStats.elements;
        primaryDensityStats.stddev = Math.pow(
                (SS - (2 * primaryDensityStats.mean * S) + (primaryDensityStats.elements * primaryDensityStats.mean
                        * primaryDensityStats.mean)) / primaryDensityStats.elements, 0.5);

        for (int i =0 ; i < fS.length; ++i) {
            primaryDensityStats.fMean[i] = fS[i] / primaryDensityStats.elements;
            primaryDensityStats.fStddev[i] = Math.pow(
                    (fSS[i] - (2 * primaryDensityStats.fMean[i] * fS[i]) + (primaryDensityStats.elements
                            * primaryDensityStats.fMean[i] * primaryDensityStats.fMean[i]))
                            / primaryDensityStats.elements, 0.5);
        }

        return closest;
    }

    public boolean isMultiClass() {
        return primaryDensityStats.multiClass;
    }

    @Getter
    public class DensityStats {

        private int elements;
        private boolean multiClass;

        private double sum;
        private double max;
        private double stddev;
        private double mean;

        private double fMin[];
        private double fMax[];
        private double fStddev[];
        private double fMean[];

        private void reset() {
            primaryDensityStats.multiClass = Utils.moreThanOneClass(Cluster.this.elements);
            int features = centroid.getFeatures().length;
            primaryDensityStats.fMin = new double[features];
            primaryDensityStats.fMax = new double[features];
            primaryDensityStats.fStddev = new double[features];
            primaryDensityStats.fMean = new double[features];
        }
    }

}
