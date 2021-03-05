package pl.edu.pw.ddm.platform.algorithms.clustering.lct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import lombok.SneakyThrows;
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

class KmeansWrapper {

    @SneakyThrows
    public SimpleKMeans performKmeans(DataProvider dataProvider, ParamProvider paramProvider) {
        Collection<Data> training = dataProvider.training();
        Data sample = training.iterator()
                .next();
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
        int seed = paramProvider.provideNumeric("seed", (double) new Random().nextInt(Integer.MAX_VALUE)).intValue();
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
