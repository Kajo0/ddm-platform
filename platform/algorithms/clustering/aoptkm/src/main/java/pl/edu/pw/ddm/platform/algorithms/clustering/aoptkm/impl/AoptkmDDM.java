package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.AutoOpticsKm;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.point.ObjectPoint;
import pl.edu.pw.ddm.platform.interfaces.algorithm.DdmPipeline;
import pl.edu.pw.ddm.platform.interfaces.algorithm.GlobalProcessor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.LocalProcessor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.CentralDdmPipeline;
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ResultCollector;
import pl.edu.pw.ddm.platform.interfaces.data.SampleProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.Clustering;

public class AoptkmDDM implements LocalProcessor<LModel, GModel, AoptkmDDM>,
        GlobalProcessor<LModel, GModel>,
        pl.edu.pw.ddm.platform.interfaces.algorithm.central.LocalProcessor<LModel>,
        pl.edu.pw.ddm.platform.interfaces.algorithm.central.GlobalProcessor<LModel, GModel>,
        pl.edu.pw.ddm.platform.interfaces.algorithm.central.LocalUpdater<LModel, GModel, AoptkmDDM>,
        pl.edu.pw.ddm.platform.interfaces.algorithm.AlgorithmConfig,
        Clustering {

    private List<ObjectPoint> globalCentroids;

    @Override
    public LModel processLocal(DataProvider dataProvider, ParamProvider paramProvider) {
        AutoOpticsKm algorithm = new AutoOpticsKm(paramProvider);
        List<ObjectPoint> pts = toObjectPoints(dataProvider.training());
        return algorithm.localClustering("dummy", pts);
    }

    @Override
    public AoptkmDDM updateLocal(LModel localModel, GModel globalModel, DataProvider dataProvider, ParamProvider paramProvider) {
        globalCentroids = globalModel.getCentroids();
        return this;
    }

    @Override
    public GModel processGlobal(Collection<LModel> localModels, ParamProvider paramProvider) {
        AutoOpticsKm algorithm = new AutoOpticsKm(paramProvider);
        return localModels.stream()
                .collect(Collectors.collectingAndThen(Collectors.toList(), algorithm::globalClustering));
    }

    @Override
    public void cluster(SampleProvider sampleProvider, ParamProvider paramProvider, ResultCollector resultCollector) {
        AutoOpticsKm algorithm = new AutoOpticsKm(paramProvider);
        List<ObjectPoint> pts = toObjectPoints(sampleProvider.all());
        algorithm.updateLocalClustering(pts, globalCentroids)
                .forEach(kmeansCluster -> kmeansCluster.cluster.forEach(obj -> resultCollector.collect(String.valueOf(obj.index), String.valueOf(kmeansCluster.centroid.clusterId))));
    }

    private List<ObjectPoint> toObjectPoints(Collection<? extends Data> data) {
        return data.stream()
                .map(d -> new ObjectPoint(toObjectAttributes(d.getNumericAttributes()), (int) Double.parseDouble(d.getId())))
                .collect(Collectors.toList());
    }

    private Object[] toObjectAttributes(double[] numericAttributes) {
        return Arrays.stream(numericAttributes)
                .boxed()
                .toArray();
    }

    @Override
    public String name() {
        return "Opt-DKM";
    }

    @Override
    public DdmPipeline pipeline() {
        return CentralDdmPipeline.builder()
                .local(this.getClass())
                .global(this.getClass())
                .lastLocal(this.getClass());
    }

}
