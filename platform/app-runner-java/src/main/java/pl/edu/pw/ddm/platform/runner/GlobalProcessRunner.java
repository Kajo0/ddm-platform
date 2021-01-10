package pl.edu.pw.ddm.platform.runner;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.spark.api.java.function.FlatMapFunction;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.GlobalProcessor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.Processor;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.model.BaseModel;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;
import pl.edu.pw.ddm.platform.runner.data.NodeParamProvider;
import pl.edu.pw.ddm.platform.runner.models.ModelWrapper;
import pl.edu.pw.ddm.platform.runner.utils.AlgorithmProcessorInitializer;
import pl.edu.pw.ddm.platform.runner.utils.ModelPersister;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
class GlobalProcessRunner implements FlatMapFunction<Iterator<LocalModel>, ModelWrapper> {

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

        GlobalProcessor<LocalModel, GlobalModel> gp = AlgorithmProcessorInitializer.initProcessor(
                initParams.getAlgorithmPackageName(),
                (Class<GlobalProcessor<LocalModel, GlobalModel>>) processor,
                GlobalProcessor.class
        );

        LocalDateTime start = LocalDateTime.now();
        GlobalModel globalModel = gp.processGlobal(models, paramProvider);
        LocalDateTime end = LocalDateTime.now();

        ModelPersister.saveGlobal(initParams.getExecutionPath(), globalModel, stageIndex, initParams.getExecutionId());

        ModelWrapper wrapper = ModelWrapper.global(globalModel, InetAddress.getLocalHost().toString());
        wrapper.getTimeStatistics().setStart(start);
        wrapper.getTimeStatistics().setEnd(end);

        Optional.of(globalModel)
                .map(BaseModel::customMetrics)
                .ifPresent(wrapper.getDatasetStatistics()::setCustomMetrics);

        return new SingletonIterator(wrapper);
    }

}
