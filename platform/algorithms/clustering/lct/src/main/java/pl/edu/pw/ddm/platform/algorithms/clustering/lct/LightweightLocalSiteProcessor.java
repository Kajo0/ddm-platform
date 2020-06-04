package pl.edu.pw.ddm.platform.algorithms.clustering.lct;

import java.util.Arrays;
import java.util.stream.DoubleStream;

import pl.edu.pw.ddm.platform.interfaces.algorithm.LocalProcessor;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.Clustering;
import weka.clusterers.SimpleKMeans;
import weka.core.Instance;

public class LightweightLocalSiteProcessor implements LocalProcessor<LModel, GModel, Clustering> {

    @Override
    public LModel processLocal(DataProvider dataProvider, ParamProvider paramProvider) {
        LModel.LModelBuilder builder = LModel.builder();
        SimpleKMeans kmeans = new KmeansWrapper().performKmeans(dataProvider, paramProvider);

        double[] variances = countVariances(kmeans);
        double[] sizes = kmeans.getClusterSizes();
        for (int i = 0; i < kmeans.getNumClusters(); ++i) {
            double[] centroid = kmeans.getClusterCentroids()
                    .get(i)
                    .toDoubleArray();
            builder.cluster(LModel.LocalCluster.of(centroid, (int) sizes[i], variances[i]));
        }

        return builder.build();
    }

    @Override
    public Clustering updateLocal(LModel lModel, GModel gModel, DataProvider dataProvider, ParamProvider paramProvider) {
        return new LightweightClusterer(gModel);
    }

    private double[] countVariances(SimpleKMeans kmeans) {
        return kmeans.getClusterStandardDevs()
                .stream()
                .map(Instance::toDoubleArray)
                .map(this::square)
                .map(stdDevs -> {
                    double variance = DoubleStream.of(stdDevs)
                            .map(sd -> sd * sd)
                            .sum();
                    return Double.isNaN(variance) ? 0 : variance;
                })
                .mapToDouble(d -> d)
                .toArray();
    }

    private double[] square(double[] doubles) {
        return Arrays.stream(doubles)
                .map(d -> d * d)
                .toArray();
    }

}
