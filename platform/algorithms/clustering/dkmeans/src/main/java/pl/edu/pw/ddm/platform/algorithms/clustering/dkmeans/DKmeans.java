package pl.edu.pw.ddm.platform.algorithms.clustering.dkmeans;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import pl.edu.pw.ddm.platform.distfunc.EuclideanDistance;
import pl.edu.pw.ddm.platform.interfaces.algorithm.AlgorithmConfig;
import pl.edu.pw.ddm.platform.interfaces.algorithm.DdmPipeline;
import pl.edu.pw.ddm.platform.interfaces.algorithm.GlobalProcessor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.LocalProcessor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.CentralDdmPipeline;
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ResultCollector;
import pl.edu.pw.ddm.platform.interfaces.data.SampleData;
import pl.edu.pw.ddm.platform.interfaces.data.SampleProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.Clustering;
import weka.clusterers.SimpleKMeans;

public class DKmeans implements LocalProcessor<LModel, GModel, Clustering>,
        GlobalProcessor<LModel, GModel>,
        pl.edu.pw.ddm.platform.interfaces.algorithm.central.LocalProcessor<LModel>,
        pl.edu.pw.ddm.platform.interfaces.algorithm.central.GlobalProcessor<LModel, GModel>,
        pl.edu.pw.ddm.platform.interfaces.algorithm.central.LocalUpdater<LModel, GModel, DKmeans>,
        AlgorithmConfig,
        Clustering {

    private GModel methodModel;

    @Override
    public LModel processLocal(DataProvider dataProvider, ParamProvider paramProvider) {
        LModel.LModelBuilder builder = LModel.builder();
        Collection<double[]> training = dataProvider.training()
                .stream()
                .map(Data::getNumericAttributes)
                .collect(Collectors.toList());
        SimpleKMeans kmeans = new KmeansWrapper().performKmeans(training, paramProvider);

        for (int i = 0; i < kmeans.getNumClusters(); ++i) {
            double[] centroid = kmeans.getClusterCentroids()
                    .get(i)
                    .toDoubleArray();
            builder.centroid(centroid);
        }

        return builder.build();
    }

    @Override
    public DKmeans updateLocal(LModel lModel, GModel gModel, DataProvider dataProvider, ParamProvider paramProvider) {
        methodModel = gModel;
        return this;
    }

    @Override
    public GModel processGlobal(Collection<LModel> localModels, ParamProvider paramProvider) {
        GModel.GModelBuilder builder = GModel.builder();
        Collection<double[]> centroids = localModels.stream()
                .map(LModel::getCentroids)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        SimpleKMeans kmeans = new KmeansWrapper().performKmeans(centroids, paramProvider);

        for (int i = 0; i < kmeans.getNumClusters(); ++i) {
            double[] centroid = kmeans.getClusterCentroids()
                    .get(i)
                    .toDoubleArray();
            builder.centroid(GModel.GlobalCluster.of(centroid, i));
        }

        return builder.build();
    }

    @Override
    public void cluster(SampleProvider sampleProvider, ParamProvider paramProvider, ResultCollector resultCollector) {
        DistanceFunction distanceFunction;
        if (paramProvider.distanceFunction() != null) {
            distanceFunction = paramProvider.distanceFunction();
        } else {
            System.out.println("No distance function provided - using Euclidean as default.");
            distanceFunction = new EuclideanDistance();
        }

        sampleProvider.forEachRemaining(sample -> resultCollector.collect(sample.getId(), findClosest(distanceFunction, sample)));
    }

    private Integer findClosest(DistanceFunction distanceFunction, SampleData sample) {
        return methodModel.getClusters()
                .stream()
                .min(Comparator.comparingDouble(c -> distanceFunction.distance(sample.getNumericAttributes(), c.getCentroid())))
                .map(GModel.GlobalCluster::getLabel)
                .orElse(null);
    }

    @Override
    public String name() {
        return "DK-means";
    }

    @Override
    public DdmPipeline pipeline() {
        return CentralDdmPipeline.builder()
                .local(this.getClass())
                .global(this.getClass())
                .lastLocal(this.getClass());
    }

}
