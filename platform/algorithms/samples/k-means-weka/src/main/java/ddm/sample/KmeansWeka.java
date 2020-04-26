package ddm.sample;

import java.util.Collection;

import lombok.SneakyThrows;
import pl.edu.pw.ddm.platform.interfaces.algorithm.GlobalProcessor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.LocalProcessor;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.Clustering;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;

public class KmeansWeka implements LocalProcessor<LGModel, LGModel, Clustering>, GlobalProcessor<LGModel, LGModel> {

    @Override
    public LGModel processLocal(DataProvider dataProvider, ParamProvider paramProvider) {
        return new LGModel();
    }

    @SneakyThrows
    @Override
    public Clustering updateLocal(LGModel lModel, LGModel gModel, DataProvider dataProvider, ParamProvider paramProvider) {
        if ("true".equals(paramProvider.provide("preCalcCentroids"))) {
            SimpleKMeans kmeans = Clusterer.performNewClustering(dataProvider.training(), paramProvider);
            Instances centroids = kmeans.getClusterCentroids();

            return new Clusterer(centroids, kmeans.getDistanceFunction());
        } else {
            return new Clusterer();
        }
    }

    @Override
    public LGModel processGlobal(Collection<LGModel> localModels, ParamProvider paramProvider) {
        return new LGModel();
    }


}
