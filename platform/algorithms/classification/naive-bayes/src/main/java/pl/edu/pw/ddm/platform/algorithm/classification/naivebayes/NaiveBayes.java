package pl.edu.pw.ddm.platform.algorithm.classification.naivebayes;

import pl.edu.pw.ddm.platform.interfaces.algorithm.AlgorithmConfig;
import pl.edu.pw.ddm.platform.interfaces.algorithm.DdmPipeline;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.CentralDdmPipeline;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.GlobalUpdater;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.LocalProcessor;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;

import java.util.Collection;

public class NaiveBayes implements LocalProcessor<LModel>,
        GlobalUpdater<LModel, GlobalClassifier>,
        AlgorithmConfig {

    @Override
    public LModel processLocal(DataProvider dataProvider, ParamProvider paramProvider) {
        System.out.println("  [[FUTURE LOG]] processLocal");

        int attrAmount = dataProvider.getDataDescription().getAttributesAmount();

        return new StatsCollector(dataProvider.training(), attrAmount).collect();
    }

    @Override
    public GlobalClassifier updateGlobal(Collection<LModel> localModels, ParamProvider paramProvider) {
        System.out.println("  [[FUTURE LOG]] updateGlobal");

        int attrAmount = localModels.stream()
                .map(LModel::getAttributesAmount)
                .findFirst()
                .orElse(-1);
        GModel model = new StatsAggregator(localModels, attrAmount).aggregate();

        return new GlobalClassifier(model);
    }

    @Override
    public DdmPipeline pipeline() {
        return CentralDdmPipeline.builder()
                .local(NaiveBayes.class)
                .lastGlobal(NaiveBayes.class);
    }

}
