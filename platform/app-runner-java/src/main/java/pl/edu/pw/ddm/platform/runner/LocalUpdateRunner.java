package pl.edu.pw.ddm.platform.runner;

import java.net.InetAddress;
import java.util.Iterator;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.spark.api.java.function.FlatMapFunction;
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

    private final String dataId;
    private final String executionId;

    @Override
    public Iterator<ModelWrapper> call(Iterator<GlobalModel> iterator) throws Exception {
        Integer id = PersistentIdStamper.read();

        NodeDataProvider dataProvider = new NodeDataProvider(dataId);
        ParamProvider paramProvider = new NodeParamProvider();

        LocalModel previousModel = ModelPersister.loadLocal(executionId);
        MiningMethod method = AlgorithmProcessorInitializer.initLocalProcessor()
                .updateLocal(previousModel, iterator.next(), dataProvider, paramProvider);
        MethodPersister.save(method, executionId);

        LocalModel model = new StringLocalModel("ackTime=" + System.currentTimeMillis());
        ModelWrapper wrapper = ModelWrapper.local(model, InetAddress.getLocalHost().toString(), id);
        return new SingletonIterator(wrapper);
    }

}
