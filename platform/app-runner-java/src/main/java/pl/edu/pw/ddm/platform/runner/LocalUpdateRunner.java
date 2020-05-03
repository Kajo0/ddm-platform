package pl.edu.pw.ddm.platform.runner;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Iterator;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.spark.api.java.function.FlatMapFunction;
import pl.edu.pw.ddm.platform.interfaces.algorithm.LocalProcessor;
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

@AllArgsConstructor(access = AccessLevel.PACKAGE)
class LocalUpdateRunner implements FlatMapFunction<Iterator<GlobalModel>, ModelWrapper> {

    private final InitParamsDto initParams;

    @Override
    public Iterator<ModelWrapper> call(Iterator<GlobalModel> iterator) throws Exception {
        Integer id = PersistentIdStamper.read();

        NodeDataProvider dataProvider = new NodeDataProvider(initParams.getTrainDataId(), initParams.getTestDataId());
        ParamProvider paramProvider = new NodeParamProvider(initParams.findDistanceFunction(), initParams.getExecutionParams());

        LocalModel previousModel = ModelPersister.loadLocal(initParams.getExecutionId());
        LocalProcessor processor = AlgorithmProcessorInitializer.initLocalProcessor(initParams.getAlgorithmPackageName());

        LocalDateTime start = LocalDateTime.now();
        MiningMethod method = processor.updateLocal(previousModel, iterator.next(), dataProvider, paramProvider);
        LocalDateTime end = LocalDateTime.now();

        MethodPersister.save(method, initParams.getExecutionId());

        LocalModel model = new StringLocalModel("ackTime=" + System.currentTimeMillis());
        ModelWrapper wrapper = ModelWrapper.local(model, InetAddress.getLocalHost().toString(), id);
        wrapper.getTimeStatistics().setStart(start);
        wrapper.getTimeStatistics().setEnd(end);
        wrapper.getTimeStatistics().setDataLoadingMillis(dataProvider.getLoadingMillis());

        return new SingletonIterator(wrapper);
    }

}
