package ddm.sample;

import lombok.SneakyThrows;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.LocalUpdater;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;

public class KmeansWeka implements LocalUpdater<LGModel, LGModel, Clusterer> {

    @SneakyThrows
    @Override
    public Clusterer updateLocal(LGModel lModel, LGModel gModel, DataProvider dataProvider, ParamProvider paramProvider) {
        if ("true".equals(paramProvider.provide("preCalcCentroids"))) {
            SimpleKMeans kmeans = Clusterer.performNewClustering(dataProvider.training(), paramProvider);
            Instances centroids = kmeans.getClusterCentroids();

            return new Clusterer(centroids, kmeans.getDistanceFunction());
        } else {
            return new Clusterer();
        }
    }

}
