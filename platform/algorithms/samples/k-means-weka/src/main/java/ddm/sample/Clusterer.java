package ddm.sample;

import java.util.ArrayList;
import java.util.Collection;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ResultCollector;
import pl.edu.pw.ddm.platform.interfaces.data.SampleProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.Clustering;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.DistanceFunction;
import weka.core.Instances;

@NoArgsConstructor
@AllArgsConstructor
public class Clusterer implements Clustering {

    private Instances centroids;
    private DistanceFunction wekaDistanceFunction;

    @SneakyThrows
    @Override
    public void cluster(SampleProvider sampleProvider, ParamProvider paramProvider, ResultCollector resultCollector) {
        if (centroids != null) {
            assignToClosestCentroids(sampleProvider, resultCollector);
        } else {
            SimpleKMeans kmeans = performNewClustering(sampleProvider.all(), paramProvider);

            int[] assignments = kmeans.getAssignments();
            for (int assignment : assignments) {
                resultCollector.collect(String.valueOf(sampleProvider.next().getId()), String.valueOf(assignment));
            }
        }
    }

    private void assignToClosestCentroids(SampleProvider sampleProvider, ResultCollector resultCollector) {
        sampleProvider.all()
                .stream()
                .map(sample -> new DenseInstance(1.0, sample.getNumericAttributes()))
                .forEach(sample -> {
                    double minDistance = Double.MAX_VALUE;
                    int minCentroid = -1;
                    for (int i = 0; i < centroids.size(); ++i) {
                        double distance = wekaDistanceFunction.distance(centroids.get(i), sample);
                        if (distance < minDistance) {
                            minCentroid = i;
                            minDistance = distance;
                        }
                    }
                    resultCollector.collect(sampleProvider.next().getId(), String.valueOf(minCentroid));
                });
    }

    static SimpleKMeans performNewClustering(Collection<? extends Data> training, ParamProvider paramProvider) throws Exception {
        Data sample = training.iterator().next();
        ArrayList<Attribute> attrs = new ArrayList<>();
        for (int i = 0; i < sample.getNumericAttributes().length; ++i) {
            attrs.add(new Attribute(String.valueOf(i)));
        }

        Instances dataset = new Instances("evaluationSet", attrs, training.size());
        for (Data d : training) {
            DenseInstance di = new DenseInstance(1.0, d.getNumericAttributes());
            di.setDataset(dataset);
            dataset.add(di);
        }

        SimpleKMeans kmeans = new SimpleKMeans();
        kmeans.setNumClusters(paramProvider.provideNumeric("groups").intValue());
        kmeans.setMaxIterations(paramProvider.provideNumeric("iterations").intValue());
        kmeans.setPreserveInstancesOrder(true);

        kmeans.buildClusterer(dataset);

        return kmeans;
    }

    @Override
    public String name() {
        return "WEKA K-means";
    }

}
