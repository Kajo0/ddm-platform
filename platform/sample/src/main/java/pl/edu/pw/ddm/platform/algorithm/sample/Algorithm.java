package pl.edu.pw.ddm.platform.algorithm.sample;

import java.util.Collection;
import java.util.Random;

import pl.edu.pw.ddm.platform.interfaces.algorithm.GlobalProcessor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.LocalProcessor;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.Classifier;

public class Algorithm implements LocalProcessor<LModel, GModel, Classifier>, GlobalProcessor<LModel, GModel> {

    @Override
    public LModel processLocal(DataProvider dataProvider, ParamProvider paramProvider) {
        int rand = new Random().nextInt(10);
        System.out.println("Algorithm 1. stage local process: " + rand);
        return new LModel(rand);
    }

    @Override
    public Classifier updateLocal(LModel localModel, GModel globalModel, DataProvider dataProvider, ParamProvider paramProvider) {
        System.out.println("Algorithm 3. stage local process: " + (globalModel.getSum() / (double) localModel.getNum()));
        return (sampleProvider, resultCollector) -> {
        };
    }

    @Override
    public GModel processGlobal(Collection<LModel> localModels, ParamProvider paramProvider) {
        int sum = localModels.stream()
                .map(LModel::getNum)
                .reduce(0, Integer::sum);
        System.out.println("Algorithm 2. stage global process: " + sum);
        return new GModel(sum);
    }

}
