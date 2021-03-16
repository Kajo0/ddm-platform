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
import weka.core.SelectedTag;
import weka.core.Tag;

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
        String initMethod = paramProvider.provide("init_kmeans_method", "Random");
        SelectedTag method = findSelectedTag(initMethod);

        printConfig(groups, iterations, seed, initMethod);

        SimpleKMeans kmeans = new SimpleKMeans();
        kmeans.setNumClusters(groups);
        kmeans.setMaxIterations(iterations);
        kmeans.setPreserveInstancesOrder(true);
        kmeans.setSeed(seed);
        kmeans.setDisplayStdDevs(true);

        if (initMethod != null) {
            kmeans.setInitializationMethod(method);
        }

        kmeans.buildClusterer(dataset);

        return kmeans;
    }

    private SelectedTag findSelectedTag(String initMethod) {
        for (Tag tag : SimpleKMeans.TAGS_SELECTION) {
            if (tag.getReadable().equals(initMethod)) {
                System.out.println("  [[FUTURE LOG]] Found tag method for '" + initMethod + "'");
                return new SelectedTag(tag.getID(), SimpleKMeans.TAGS_SELECTION);
            }
        }
        System.out.println("  [[FUTURE LOG]] Not found tag method for '" + initMethod + "' so using default.");
        return null;
    }

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

}
