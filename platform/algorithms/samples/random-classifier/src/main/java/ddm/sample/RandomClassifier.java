package ddm.sample;

import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import pl.edu.pw.ddm.platform.interfaces.algorithm.AlgorithmConfig;
import pl.edu.pw.ddm.platform.interfaces.algorithm.DdmPipeline;
import pl.edu.pw.ddm.platform.interfaces.algorithm.GlobalProcessor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.LocalProcessor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.CentralDdmPipeline;
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ResultCollector;
import pl.edu.pw.ddm.platform.interfaces.data.SampleData;
import pl.edu.pw.ddm.platform.interfaces.data.SampleProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.Classifier;

public class RandomClassifier implements LocalProcessor<LModel, GModel, Classifier>,
        GlobalProcessor<LModel, GModel>,
        pl.edu.pw.ddm.platform.interfaces.algorithm.central.LocalProcessor<LModel>,
        pl.edu.pw.ddm.platform.interfaces.algorithm.central.GlobalProcessor<LModel, GModel>,
        pl.edu.pw.ddm.platform.interfaces.algorithm.central.LocalUpdater<LModel, GModel, RandomClassifier>,
        AlgorithmConfig,
        Classifier {

    private Random rand = null;
    private Integer labelPercent = null;
    private GModel globalModel = null;

    @Override
    public LModel processLocal(DataProvider dataProvider, ParamProvider paramProvider) {
        // FIXME rather preprocessing than local classsifier
        Set<String> labels = dataProvider.training()
                .stream()
                .map(Data::getLabel)
                .collect(Collectors.toSet());
        System.out.println("Algorithm 1. stage local process - unique labels: " + labels);
        return new LModel(labels);
    }

    @Override
    public RandomClassifier updateLocal(LModel localModel, GModel globalModel, DataProvider dataProvider, ParamProvider paramProvider) {
        this.labelPercent = (int) ((double) localModel.getLocalLabels().size() / globalModel.getAllLabels().size() * 100);
        this.globalModel = globalModel;

        System.out.println("Algorithm 3. stage local process - percent: " + labelPercent);
        return this;
    }

    @Override
    public GModel processGlobal(Collection<LModel> localModels, ParamProvider paramProvider) {
        Set<String> allLabels = localModels.stream()
                .map(LModel::getLocalLabels)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        System.out.println("Algorithm 2. stage global process - all labels: " + allLabels);
        return new GModel(allLabels);
    }

    @Override
    public void classify(SampleProvider sampleProvider, ParamProvider paramProvider, ResultCollector resultCollector) {
        System.out.println("Algorithm 4. stage local classification for label percent: " + labelPercent);
        rand = new Random(labelPercent);
        sampleProvider.forEachRemaining(sample -> classify(sample, resultCollector));
    }

    private void classify(SampleData sampleData, ResultCollector resultCollector) {
        int index = rand.nextInt(globalModel.getAllLabels().size());
        String result = globalModel.getAllLabels()
                .stream()
                .skip(index)
                .findFirst()
                .orElse(null);
        resultCollector.collect(sampleData.getId(), result);
    }

    @Override
    public String name() {
        return "Random-Classifier";
    }

    @Override
    public DdmPipeline pipeline() {
        return CentralDdmPipeline.builder()
                .local(this.getClass())
                .global(this.getClass())
                .lastLocal(this.getClass());
    }

}
