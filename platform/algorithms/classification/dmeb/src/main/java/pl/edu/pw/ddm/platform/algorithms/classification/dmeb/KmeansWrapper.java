package pl.edu.pw.ddm.platform.algorithms.classification.dmeb;

import java.util.ArrayList;
import java.util.Collection;

import lombok.SneakyThrows;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

class KmeansWrapper {

    @SneakyThrows
    public SimpleKMeans performKmeans(Collection<double[]> data, ParamProvider paramProvider) {
        double[] sample = data.stream()
                .findFirst()
                .orElse(null);
        ArrayList<Attribute> attrs = new ArrayList<>();
        for (int i = 0; i < sample.length; ++i) {
            attrs.add(new Attribute(String.valueOf(i)));
        }

        Instances dataset = new Instances("evaluationSet", attrs, data.size());
        for (double[] d : data) {
            DenseInstance di = new DenseInstance(1.0, d);
            di.setDataset(dataset);
            dataset.add(di);
        }

        int groups = paramProvider.provideNumeric("groups").intValue();
        int iterations = paramProvider.provideNumeric("iterations").intValue();
        int seed = paramProvider.provideNumeric("seed", 1d).intValue();
        printConfig(groups, iterations, seed);

        SimpleKMeans kmeans = new SimpleKMeans();
        kmeans.setNumClusters(groups);
        kmeans.setMaxIterations(iterations);
        kmeans.setPreserveInstancesOrder(true);
        kmeans.setSeed(seed);
        kmeans.setDisplayStdDevs(true);

        kmeans.buildClusterer(dataset);

        return kmeans;
    }

    private static void printConfig(int groups, int iterations, int seed) {
        System.out.println("---------------------------------");
        System.out.println("-     K-means WEKA - CONFIG     -");
        System.out.println("---------------------------------");
        System.out.println("  groups        = " + groups);
        System.out.println("  iterations    = " + iterations);
        System.out.println("  seed          = " + seed);
        System.out.println("---------------------------------");
    }

}
