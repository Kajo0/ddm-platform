package pl.edu.pw.ddm.platform.runner;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.spark.api.java.function.FlatMapFunction;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.LocalRepeater;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.Processor;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.model.BaseModel;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;
import pl.edu.pw.ddm.platform.runner.data.NodeDataProvider;
import pl.edu.pw.ddm.platform.runner.data.NodeParamProvider;
import pl.edu.pw.ddm.platform.runner.models.ModelWrapper;
import pl.edu.pw.ddm.platform.runner.utils.AlgorithmProcessorInitializer;
import pl.edu.pw.ddm.platform.runner.utils.ModelPersister;
import pl.edu.pw.ddm.platform.runner.utils.PersistentIdStamper;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
class LocalRepeatRunner implements FlatMapFunction<Iterator<GlobalModel>, ModelWrapper> {

    private final InitParamsDto initParams;
    private final Class<? extends Processor> processor;
    private final int stageIndex;

    @Override
    public Iterator<ModelWrapper> call(Iterator<GlobalModel> iterator) throws Exception {
        Integer id = PersistentIdStamper.read();

        NodeDataProvider dataProvider = new NodeDataProvider(initParams.getDatasetsPath(), initParams.getTrainDataId(), initParams.getTestDataId());
        ParamProvider paramProvider = new NodeParamProvider(initParams.findDistanceFunction(), initParams.getExecutionParams());

        LocalModel previousModel = ModelPersister.loadLastLocal(initParams.getExecutionPath(), stageIndex, initParams.getExecutionId());

        LocalRepeater<LocalModel, LocalModel, GlobalModel> lp = AlgorithmProcessorInitializer.initProcessor(
                initParams.getAlgorithmPackageName(),
                (Class<LocalRepeater<LocalModel, LocalModel, GlobalModel>>) processor,
                LocalRepeater.class
        );

        LocalDateTime start = LocalDateTime.now();
        LocalModel model = lp.repeatLocal(iterator.next(), previousModel, dataProvider, paramProvider);
        LocalDateTime end = LocalDateTime.now();

        ModelPersister.saveLocal(initParams.getExecutionPath(), model, stageIndex, initParams.getExecutionId());

        ModelWrapper wrapper = ModelWrapper.local(model, InetAddress.getLocalHost().toString(), id);
        wrapper.getTimeStatistics().setStart(start);
        wrapper.getTimeStatistics().setEnd(end);
        wrapper.getTimeStatistics().setDataLoadingMillis(dataProvider.getLoadingMillis());

        Optional.of(model)
                .map(BaseModel::customMetrics)
                .ifPresent(wrapper.getDatasetStatistics()::setCustomMetrics);

        return new SingletonIterator(wrapper);
    }

}
