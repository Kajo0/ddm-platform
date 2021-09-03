package pl.edu.pw.ddm.platform.runner;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.spark.api.java.function.FlatMapFunction;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.GlobalUpdater;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.Processor;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.MiningMethod;
import pl.edu.pw.ddm.platform.interfaces.model.BaseModel;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;
import pl.edu.pw.ddm.platform.runner.data.NodeParamProvider;
import pl.edu.pw.ddm.platform.runner.models.ModelWrapper;
import pl.edu.pw.ddm.platform.runner.models.StringGlobalModel;
import pl.edu.pw.ddm.platform.runner.utils.AlgorithmProcessorInitializer;
import pl.edu.pw.ddm.platform.runner.utils.MethodPersister;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
class GlobalUpdateRunner implements FlatMapFunction<Iterator<LocalModel>, ModelWrapper> {

    private final InitParamsDto initParams;
    private final Class<? extends Processor> processor;
    private final int stageIndex;

    @Override
    public Iterator<ModelWrapper> call(Iterator<LocalModel> iterator) throws Exception {
        List<LocalModel> models = new ArrayList<>();
        while (iterator.hasNext()) {
            models.add(iterator.next());
        }

        ParamProvider paramProvider = new NodeParamProvider(initParams.findDistanceFunction(), initParams.getExecutionParams());

        GlobalUpdater<LocalModel, MiningMethod> gp = AlgorithmProcessorInitializer.initProcessor(
                initParams.getAlgorithmPackageName(),
                (Class<GlobalUpdater<LocalModel, MiningMethod>>) processor,
                GlobalUpdater.class
        );

        System.out.println(paramProvider.prettyPrintParams());

        LocalDateTime start = LocalDateTime.now();
        MiningMethod method = gp.updateGlobal(models, paramProvider);
        LocalDateTime end = LocalDateTime.now();

        MethodPersister.save(initParams.getExecutionPath(), method, initParams.getExecutionId());

        GlobalModel model = new StringGlobalModel("ackTime=" + System.currentTimeMillis());
        ModelWrapper wrapper = ModelWrapper.globalMethod(model, method, InetAddress.getLocalHost().toString());
        wrapper.getTimeStatistics().setStart(start);
        wrapper.getTimeStatistics().setEnd(end);

        Optional.of(model)
                .map(BaseModel::customMetrics)
                .ifPresent(wrapper.getDatasetStatistics()::setCustomMetrics);

        return new SingletonIterator(wrapper);
    }

}
