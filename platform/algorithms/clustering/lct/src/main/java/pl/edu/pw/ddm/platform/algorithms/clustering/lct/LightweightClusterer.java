package pl.edu.pw.ddm.platform.algorithms.clustering.lct;

import java.util.Comparator;

import lombok.NoArgsConstructor;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ResultCollector;
import pl.edu.pw.ddm.platform.interfaces.data.SampleData;
import pl.edu.pw.ddm.platform.interfaces.data.SampleProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.Clustering;

@NoArgsConstructor
public class LightweightClusterer implements Clustering {

    private GModel model;
    private DistanceFunction distanceFunction;

    public LightweightClusterer(GModel model) {
        this.model = model;
    }

    @Override
    public void cluster(SampleProvider sampleProvider, ParamProvider paramProvider, ResultCollector resultCollector) {
        distanceFunction = paramProvider.distanceFunction();
        sampleProvider.forEachRemaining(sample -> resultCollector.collect(sample.getId(), findClosest(sample)));
    }

    private String findClosest(SampleData sample) {
        return model.getClusters()
                .stream()
                .min(Comparator.comparingDouble(c -> distanceFunction.distance(sample.getNumericAttributes(), c.getCentroid())))
                .map(GModel.GlobalCluster::getLabel)
                .orElse(null);
    }

    @Override
    public String name() {
        return "LCT";
    }

}