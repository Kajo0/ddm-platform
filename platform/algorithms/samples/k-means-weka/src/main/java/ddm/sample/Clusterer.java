package ddm.sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import pl.edu.pw.ddm.platform.interfaces.algorithm.AlgorithmConfig;
import pl.edu.pw.ddm.platform.interfaces.algorithm.DdmPipeline;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.CentralDdmPipeline;
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
import weka.core.SelectedTag;
import weka.core.Tag;

@NoArgsConstructor
@AllArgsConstructor
public class Clusterer implements Clustering,
        AlgorithmConfig {

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

        int groups = paramProvider.provideNumeric("groups").intValue();
        int iterations = paramProvider.provideNumeric("iterations").intValue();
        String initMethod = paramProvider.provide("init_kmeans_method", "Random");
        int seed = paramProvider.provideNumeric("seed", (double) new Random().nextInt(Integer.MAX_VALUE)).intValue();
        SelectedTag method = findSelectedTag(initMethod);
        printConfig(groups, iterations, seed, initMethod);

        SimpleKMeans kmeans = new SimpleKMeans();
        kmeans.setNumClusters(groups);
        kmeans.setMaxIterations(iterations);
        kmeans.setPreserveInstancesOrder(true);

        if (initMethod != null) {
            kmeans.setInitializationMethod(method);
        }

        kmeans.buildClusterer(dataset);

        return kmeans;
    }

    private static SelectedTag findSelectedTag(String initMethod) {
        for (Tag tag : SimpleKMeans.TAGS_SELECTION) {
            if (tag.getReadable().equals(initMethod)) {
                System.out.println("  [[FUTURE LOG]] Found tag method for '" + initMethod + "'");
                return new SelectedTag(tag.getID(), SimpleKMeans.TAGS_SELECTION);
            }
        }
        System.out.println("  [[FUTURE LOG]] Not found tag method for '" + initMethod + "' so using default.");
        return null;
    }

    // TODO remove debug checker
    private static void printConfig(int groups, int iterations, int seed, String initMethod) {
        System.out.println("---------------------------------");
        System.out.println("-     K-means WEKA - CONFIG     -");
        System.out.println("---------------------------------");
        System.out.println("  groups        = " + groups);
        System.out.println("  iterations    = " + iterations);
        System.out.println("  seed          = " + seed);
        System.out.println("  initMethod    = " + initMethod);
        System.out.println("---------------------------------");
    }

    @Override
    public String name() {
        return "WEKA K-means";
    }

    @Override
    public DdmPipeline pipeline() {
        return CentralDdmPipeline.builder()
                .onlyLocal(KmeansWeka.class);
    }

    @Override
    public boolean onlySingleNode() {
        return true;
    }

}
