package pl.edu.pw.ddm.platform.algorithms.clustering.dbirch;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.edu.pw.ddm.platform.distfunc.EuclideanDistance;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ResultCollector;
import pl.edu.pw.ddm.platform.interfaces.data.SampleData;
import pl.edu.pw.ddm.platform.interfaces.data.SampleProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.Clustering;

import java.util.Comparator;

@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DBirchClusterer implements Clustering {

    private GModel model;

    @Override
    public String name() {
        return "Distributed BIRCH";
    }

    @Override
    public void cluster(SampleProvider sampleProvider, ParamProvider paramProvider, ResultCollector resultCollector) {
        System.out.println("  [[FUTURE LOG]] cluster");

        DistanceFunction func;
        if (paramProvider.distanceFunction() != null) {
            func = paramProvider.distanceFunction();
        } else {
            func = new EuclideanDistance();
        }

        sampleProvider.forEachRemaining(sample -> {
            Integer cluster = findClosest(sample, func);
            resultCollector.collect(sample.getId(), cluster);
        });
    }

    private Integer findClosest(SampleData sample, DistanceFunction func) {
        return model.getClassToCentroidMap()
                .entrySet()
                .stream()
                .map(a -> new Pair(a.getKey(), func.distance(a.getValue(), sample.getNumericAttributes())))
                .min(Comparator.comparing(a -> a.dist))
                .get()
                .cluster;
    }

    @AllArgsConstructor
    static class Pair {
        private int cluster;
        private double dist;
    }

}
