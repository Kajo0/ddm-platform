package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;

@Getter
@Setter
public class MEBCluster implements Serializable {

    private transient boolean debug;
    private transient DistanceFunction distanceFunction;

    private boolean supportCluster;
    private Centroid centroid;
    private List<LabeledObservation> clusterElementList;
    private DensityStats primaryDensityStats = new DensityStats();

    // TODO make use of the function after removing weka impl.
    public MEBCluster(DistanceFunction distanceFunction, boolean debug) {
        this.distanceFunction = distanceFunction;
        this.debug = debug;
    }

    public void setCentroid(Centroid centroid) {
        this.centroid = centroid;
        primaryDensityStats.reset();
    }

    public void markSupportCluster(Set<LabeledObservation> sVs) {
        supportCluster = containsAny(sVs);
    }

    private boolean containsAny(LabeledObservation representativeList) {
        return containsAny(Collections.singletonList(representativeList));
    }

    private boolean containsAny(Collection<LabeledObservation> representativeList) {
        for (LabeledObservation observation : representativeList) {
            if (Arrays.equals(observation.getFeatures(), centroid.getFeatures())) {
                return true;
            }
            for (LabeledObservation ce : clusterElementList) {
                if (Arrays.equals(observation.getFeatures(), ce.getFeatures())) {
                    return true;
                }
            }
        }
        return false;
    }

    public LabeledObservation squashToCentroid() {
        primaryDensityStats.elements = clusterElementList.size();
        LabeledObservation any = clusterElementList.get(0);
        clusterElementList.clear();
        LabeledObservation squashed = new LabeledObservation(-1, centroid.getFeatures(), any.getTarget());
        clusterElementList.add(squashed);
        if (debug) {
            System.out.println("  [[FUTURE LOG]] Cluster with " + primaryDensityStats.elements + " elements squashed into 1");
        }
        return squashed;
    }

    public LabeledObservation squashToMedian() {
        primaryDensityStats.elements = clusterElementList.size();
        LabeledObservation median = clusterElementList.stream()
                .sorted(Comparator.comparingDouble(d -> distanceFunction.distance(centroid.getFeatures(), d.getFeatures())))
                .skip(primaryDensityStats.elements / 2)
                .findFirst()
                .get();
        clusterElementList.clear();
        clusterElementList.add(median);
        if (debug) {
            System.out.println("  [[FUTURE LOG]] Cluster with " + primaryDensityStats.elements + " elements medianed into 1");
        }
        return median;
    }

    public List<LabeledObservation> random(double percent, Long seed) {
        Random rand = Optional.ofNullable(seed)
                .map(Random::new)
                .orElseGet(Random::new);
        Collections.shuffle(clusterElementList, rand);

        if (percent < 0) {
            calculateMetrics();
            percent = clusterDensityPercent();
            if (debug) {
                System.out.println("  [[FUTURE LOG]] Random precalculated into " + percent + " value");
            }
        }
        if (percent == 0) {
            clusterElementList.clear();
        } else {
            long limit = Math.max(1, (long) Math.ceil(clusterElementList.size() * percent));
            if (percent == 0) {
                limit = 0;
            }

            primaryDensityStats.elements = clusterElementList.size();
            clusterElementList = clusterElementList.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
            if (debug) {
                System.out.println("  [[FUTURE LOG]] Cluster with " + primaryDensityStats.elements + " reduced randomly to " + clusterElementList.size() + " elements");
            }
        }

        return clusterElementList;
    }

    private double clusterDensityPercent() {
        return (primaryDensityStats.sum / (primaryDensityStats.elements * primaryDensityStats.max)) * (
                primaryDensityStats.stddev / primaryDensityStats.mean);
    }

    public List<LabeledObservation> leaveCloseToSvs(double percent, Collection<LabeledObservation> svs) {
        primaryDensityStats.elements = clusterElementList.size();

        if (percent == 0) {
            clusterElementList.clear();
        } else {
            if (percent < 0) {
                calculateMetrics();
                percent = clusterDensityPercent();
                if (debug) {
                    System.out.println("  [[FUTURE LOG]] Close to percent precalculated into " + percent + " value");
                }
            }
            double percentf = percent;

            Set<LabeledObservation> clusterSvs = matchInnerSupportVectors(svs);
            clusterElementList = clusterElementList.stream()
                    .collect(Collectors.groupingBy(LabeledObservation::getTarget, Collectors.toSet()))
                    .entrySet()
                    .stream()
                    .filter(e -> e.getKey() != -1)
                    .map(Map.Entry::getValue)
                    .map(classList -> classList.stream()
                            .sorted(Comparator.comparingDouble(d -> clusterSvs.stream()
                                    .map(sv -> distanceFunction.distance(d.getFeatures(), sv.getFeatures()))
                                    .reduce(Double::sum)
                                    .orElse(0d)))
                            .limit((long) Math.max(1, Math.ceil(classList.size() * percentf)))
                            .collect(Collectors.toSet()))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            if (debug) {
                System.out.println("  [[FUTURE LOG]] Cluster with " + primaryDensityStats.elements + " reduced by svs close to " + clusterElementList.size() + " elements");
            }
        }

        return clusterElementList;
    }

    private Set<LabeledObservation> matchInnerSupportVectors(Collection<LabeledObservation> svs) {
        return svs.stream()
                .filter(this::containsAny)
                .collect(Collectors.toSet());
    }

    public List<LabeledObservation> leaveBorder() {
        LabeledObservation closest = calculateMetrics();
        if (debug) {
            System.out.println("  [[FUTURE LOG]] Cluster with " + primaryDensityStats.elements + " elements [max="
                    + primaryDensityStats.max + ", sd=" + primaryDensityStats.stddev + "]");
        }

        double threshold = primaryDensityStats.max - primaryDensityStats.stddev;
        clusterElementList.removeIf(p -> {
            double dist = distanceFunction.distance(centroid.getFeatures(), p.getFeatures());
            return dist < threshold && dist > primaryDensityStats.stddev;
        });
        if (closest != null && !clusterElementList.contains(closest)) {
            clusterElementList.add(closest);
            if (debug) {
                System.out.println("  [[FUTURE LOG]] Adding closest to centroid observation");
            }
        }
        if (debug) {
            System.out.println(
                    "  [[FUTURE LOG]] Cluster with " + primaryDensityStats.elements + " reduced to border with "
                            + clusterElementList.size() + " elements");
        }
        return clusterElementList;
    }

    public LabeledObservation calculateMetrics() {
        primaryDensityStats.elements = clusterElementList.size();

        primaryDensityStats.max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        double S = 0;
        double SS = 0;
        double[] fS = new double[primaryDensityStats.fMax.length];
        double[] fSS = new double[primaryDensityStats.fMax.length];
        LabeledObservation closest = null;

        for (LabeledObservation p : clusterElementList) {
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
            primaryDensityStats.multiClass = Utils.moreThanOneClass(clusterElementList);
            int features = centroid.getFeatures().length;
            primaryDensityStats.fMin = new double[features];
            primaryDensityStats.fMax = new double[features];
            primaryDensityStats.fStddev = new double[features];
            primaryDensityStats.fMean = new double[features];
        }
    }

}
