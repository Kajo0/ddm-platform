package pl.edu.pw.ddm.platform.algorithm.sample;

import java.util.Collection;
import java.util.Random;

import pl.edu.pw.ddm.platform.interfaces.algorithm.GlobalProcessor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.LocalProcessor;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

public class Algorithm implements LocalProcessor, GlobalProcessor {

    @Override
    public LocalModel process(DataProvider dataProvider, ParamProvider paramProvider) {
        int rand = new Random().nextInt(10);
        System.out.println("Algorithm local process: " + rand);
        return new LModel(rand);
    }

    @Override
    public GlobalModel process(Collection<LocalModel> localModels, DataProvider dataProvider,
            ParamProvider paramProvider) {
        int sum = localModels.stream()
                .map(a -> (LModel) a)
                .map(LModel::getNum)
                .reduce(0, Integer::sum);
        System.out.println("Algorithm global process: " + sum);
        return new GModel(sum);
    }

}
