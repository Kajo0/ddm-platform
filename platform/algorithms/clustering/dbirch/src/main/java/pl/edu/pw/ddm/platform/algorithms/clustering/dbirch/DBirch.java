package pl.edu.pw.ddm.platform.algorithms.clustering.dbirch;

import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.birch.BIRCHLeafClustering;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.birch.CFTree;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.birch.CentroidEuclideanDistance;
import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.model.MeanModel;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.utilities.ELKIBuilder;
import lombok.NoArgsConstructor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.GlobalUpdater;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.LocalProcessor;
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.Clustering;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@NoArgsConstructor
public class DBirch implements LocalProcessor<LModel>, GlobalUpdater<LModel, Clustering> {

    @Override
    public LModel processLocal(DataProvider dataProvider, ParamProvider paramProvider) {
        System.out.println("  [[FUTURE LOG]] processLocal");

        Collection<Data> train = dataProvider.training();
        int count = train.size();
        double[][] data = new double[count][];
        String[] labels = new String[count];
        final int[] i = new int[]{0};
        train.stream()
                .sorted(Comparator.comparing(Data::getId))
                .forEach(d -> {
                    data[i[0]] = d.getNumericAttributes();
                    labels[i[0]++] = d.getLabel();
                });

        // TODO custom impl or make it available to use one of predefined distance functions as used in clustering phase
        BIRCHLeafClustering birch = new ELKIBuilder<>(BIRCHLeafClustering.class)
                .with(CFTree.Factory.Parameterizer.BRANCHING_ID, paramProvider.provideNumeric("branching_factor", 50d))
                .with(CFTree.Factory.Parameterizer.THRESHOLD_ID, paramProvider.provideNumeric("threshold", 0.5))
                .with(CFTree.Factory.Parameterizer.MAXLEAVES_ID, paramProvider.provideNumeric("groups"))
                .with(CFTree.Factory.Parameterizer.DISTANCE_ID, CentroidEuclideanDistance.class)
                .build();
        ArrayAdapterDatabaseConnection connection = new ArrayAdapterDatabaseConnection(data, labels);
        StaticArrayDatabase db = new StaticArrayDatabase(connection);
        db.initialize();
        de.lmu.ifi.dbs.elki.data.Clustering<MeanModel> result = birch.run(db);

        List<double[]> means = result.getAllClusters()
                .stream()
                .map(Cluster::getModel)
                .map(MeanModel::getMean)
                .collect(Collectors.toList());
        return new LModel(means);
    }

    @Override
    public Clustering updateGlobal(Collection<LModel> models, ParamProvider paramProvider) {
        System.out.println("  [[FUTURE LOG]] updateGlobal");

        List<double[]> newD = models.stream()
                .map(LModel::getMeans)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(a -> a[0]))
                .collect(Collectors.toList());

        int count = newD.size();
        double[][] data = new double[count][];
        String[] labels = new String[count];
        final int[] i = new int[]{0};
        newD.forEach(d -> {
            data[i[0]] = d;
            labels[i[0]++] = "0";
        });

        // TODO custom impl or make it available to use one of predefined distance functions as used in clustering phase
        BIRCHLeafClustering birch = new ELKIBuilder<>(BIRCHLeafClustering.class)
                .with(CFTree.Factory.Parameterizer.BRANCHING_ID, paramProvider.provideNumeric("branching_factor", 50d))
                .with(CFTree.Factory.Parameterizer.THRESHOLD_ID, paramProvider.provideNumeric("g_threshold", 0.5))
                .with(CFTree.Factory.Parameterizer.MAXLEAVES_ID, paramProvider.provideNumeric("g_groups"))
                .with(CFTree.Factory.Parameterizer.DISTANCE_ID, CentroidEuclideanDistance.class)
                .build();
        ArrayAdapterDatabaseConnection connection = new ArrayAdapterDatabaseConnection(data, labels);
        StaticArrayDatabase db = new StaticArrayDatabase(connection);
        db.initialize();
        de.lmu.ifi.dbs.elki.data.Clustering<MeanModel> result = birch.run(db);

        AtomicInteger in = new AtomicInteger(0);
        Map<Integer, double[]> res = new HashMap<>(result.getAllClusters().size());
        result.getAllClusters()
                .stream()
                .map(Cluster::getModel)
                .map(MeanModel::getMean)
                .forEach(m -> res.put(in.getAndIncrement(), m));

        GModel global = new GModel(res);
        return new DBirchClusterer(global);
    }

}
