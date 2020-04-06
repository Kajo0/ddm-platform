package pl.edu.pw.ddm.platform.runner;

import java.net.InetAddress;
import java.util.Iterator;

import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.spark.api.java.function.FlatMapFunction;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;
import pl.edu.pw.ddm.platform.runner.data.NodeDataProvider;
import pl.edu.pw.ddm.platform.runner.data.NodeParamProvider;
import pl.edu.pw.ddm.platform.runner.models.ModelWrapper;
import pl.edu.pw.ddm.platform.runner.utils.AlgorithmProcessorInitializer;
import pl.edu.pw.ddm.platform.runner.utils.ModelPersister;
import pl.edu.pw.ddm.platform.runner.utils.PersistentIdStamper;

class LocalProcessRunner implements FlatMapFunction<Iterator<Integer>, ModelWrapper> {

    private final String dataId;

    LocalProcessRunner(String dataId) {
        this.dataId = dataId;
    }

    @Override
    public Iterator<ModelWrapper> call(Iterator<Integer> iterator) throws Exception {
        Integer id = iterator.next();
        PersistentIdStamper.save(id);

        NodeDataProvider dataProvider = new NodeDataProvider(dataId);
        ParamProvider paramProvider = new NodeParamProvider();

        LocalModel model = AlgorithmProcessorInitializer.initLocalProcessor()
                .processLocal(dataProvider, paramProvider);
        ModelPersister.saveLocal(model);

        ModelWrapper wrapper = ModelWrapper.local(model, InetAddress.getLocalHost().toString(), id);
        return new SingletonIterator(wrapper);
    }

}
