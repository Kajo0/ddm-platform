package pl.edu.pw.ddm.platform.algorithm.sample;

import java.util.Collection;
import java.util.Random;

import pl.edu.pw.ddm.platform.interfaces.algorithm.GlobalProcessor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.LocalProcessor;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ResultCollector;
import pl.edu.pw.ddm.platform.interfaces.data.SampleData;
import pl.edu.pw.ddm.platform.interfaces.data.SampleProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.Classifier;

public class Algorithm implements LocalProcessor<LModel, GModel, Classifier>, GlobalProcessor<LModel, GModel>, Classifier {

    private Double computedValue = null;
    private GModel globalModel = null;

    @Override
    public LModel processLocal(DataProvider dataProvider, ParamProvider paramProvider) {
        int rand = new Random().nextInt(10);
        System.out.println("Algorithm 1. stage local process: " + rand);
        return new LModel(rand);
    }

    @Override
    public Classifier updateLocal(LModel localModel, GModel globalModel, DataProvider dataProvider, ParamProvider paramProvider) {
        this.computedValue = (globalModel.getSum() / (double) localModel.getNum());
        this.globalModel = globalModel;

        System.out.println("Algorithm 3. stage local process: " + computedValue);
        return this;
    }

    @Override
    public GModel processGlobal(Collection<LModel> localModels, ParamProvider paramProvider) {
        int sum = localModels.stream()
                .map(LModel::getNum)
                .reduce(0, Integer::sum);
        System.out.println("Algorithm 2. stage global process: " + sum);
        return new GModel(sum);
    }

    @Override
    public void classify(SampleProvider sampleProvider, ResultCollector resultCollector) {
        System.out.println("Algorithm 4. stage local classification");
        if (resultCollector != null) {
            if (sampleProvider != null) {
                resultCollector.collect("VALUES", computedValue + " / " + globalModel.getSum());
                sampleProvider.forEachRemaining(sample -> classify(sample, resultCollector));
            }
        }
    }

    private void classify(SampleData sampleData, ResultCollector resultCollector) {
        boolean result = sampleData.getNumericAttribute(0) == globalModel.getSum() ||
                sampleData.getNumericAttribute(0) >= computedValue &&
                        sampleData.getNumericAttribute(0) < computedValue + 1;
        resultCollector.collect(sampleData.getId(), "" + result);
    }

}
