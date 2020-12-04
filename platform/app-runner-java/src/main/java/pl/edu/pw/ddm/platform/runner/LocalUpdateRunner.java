package pl.edu.pw.ddm.platform.runner;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.spark.api.java.function.FlatMapFunction;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.LocalUpdater;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.Processor;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.MiningMethod;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;
import pl.edu.pw.ddm.platform.runner.data.NodeDataProvider;
import pl.edu.pw.ddm.platform.runner.data.NodeParamProvider;
import pl.edu.pw.ddm.platform.runner.models.ModelWrapper;
import pl.edu.pw.ddm.platform.runner.models.StringLocalModel;
import pl.edu.pw.ddm.platform.runner.utils.AlgorithmProcessorInitializer;
import pl.edu.pw.ddm.platform.runner.utils.MethodPersister;
import pl.edu.pw.ddm.platform.runner.utils.ModelPersister;
import pl.edu.pw.ddm.platform.runner.utils.PersistentIdStamper;
import pl.edu.pw.ddm.platform.runner.utils.TransferSizeUtil;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
class LocalUpdateRunner implements FlatMapFunction<Iterator<GlobalModel>, ModelWrapper> {

    private final InitParamsDto initParams;
    private final Class<? extends Processor> processor;
    private final int stageIndex;

    @Override
    public Iterator<ModelWrapper> call(Iterator<GlobalModel> iterator) throws Exception {
        Integer id = PersistentIdStamper.read();

        NodeDataProvider dataProvider = new NodeDataProvider(initParams.getDatasetsPath(), initParams.getTrainDataId(), initParams.getTestDataId());
        ParamProvider paramProvider = new NodeParamProvider(initParams.findDistanceFunction(), initParams.getExecutionParams());

        LocalModel previousModel = ModelPersister.loadLastLocal(initParams.getExecutionPath(), stageIndex, initParams.getExecutionId());

        LocalUpdater<LocalModel, GlobalModel, MiningMethod> lp = AlgorithmProcessorInitializer.initProcessor(
                initParams.getAlgorithmPackageName(),
                (Class<LocalUpdater<LocalModel, GlobalModel, MiningMethod>>) processor,
                LocalUpdater.class
        );

        LocalDateTime start = LocalDateTime.now();
        MiningMethod method = lp.updateLocal(previousModel, iterator.next(), dataProvider, paramProvider);
        LocalDateTime end = LocalDateTime.now();

        MethodPersister.save(initParams.getExecutionPath(), method, initParams.getExecutionId());

        LocalModel model = new StringLocalModel("ackTime=" + System.currentTimeMillis());
        ModelWrapper wrapper = ModelWrapper.local(model, InetAddress.getLocalHost().toString(), id);
        wrapper.getTimeStatistics().setStart(start);
        wrapper.getTimeStatistics().setEnd(end);
        wrapper.getTimeStatistics().setDataLoadingMillis(dataProvider.getLoadingMillis());

        // TODO FIXME not always required when localProcess is present
        wrapper.getDatasetStatistics().setTrainingSamplesAmount(dataProvider.trainingSize());
        Optional.of(dataProvider)
                .map(NodeDataProvider::trainingSample)
                .map(TransferSizeUtil::sizeOf)
                .ifPresent(wrapper.getDatasetStatistics()::setAvgSampleSize);

        return new SingletonIterator(wrapper);
    }

}
