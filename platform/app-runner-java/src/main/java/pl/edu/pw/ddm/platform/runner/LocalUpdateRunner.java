package pl.edu.pw.ddm.platform.runner;

import java.net.InetAddress;
import java.util.Iterator;

import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.spark.api.java.function.FlatMapFunction;
import pl.edu.pw.ddm.platform.interfaces.mining.MiningMethod;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;
import pl.edu.pw.ddm.platform.runner.models.ModelWrapper;
import pl.edu.pw.ddm.platform.runner.models.StringLocalModel;
import pl.edu.pw.ddm.platform.runner.utils.AlgorithmProcessorInitializer;
import pl.edu.pw.ddm.platform.runner.utils.MethodPersister;
import pl.edu.pw.ddm.platform.runner.utils.ModelPersister;
import pl.edu.pw.ddm.platform.runner.utils.PersistentIdStamper;

class LocalUpdateRunner implements FlatMapFunction<Iterator<GlobalModel>, ModelWrapper> {

    @Override
    public Iterator<ModelWrapper> call(Iterator<GlobalModel> iterator) throws Exception {
        Integer id = PersistentIdStamper.read();

        LocalModel previousModel = ModelPersister.loadLocal();
        MiningMethod method = AlgorithmProcessorInitializer.initLocalProcessor()
                .updateLocal(previousModel, iterator.next(), null, null);
        MethodPersister.save(method);

        LocalModel model = new StringLocalModel("ackTime=" + System.currentTimeMillis());
        ModelWrapper wrapper = ModelWrapper.local(model, InetAddress.getLocalHost().toString(), id);
        return new SingletonIterator(wrapper);
    }

}
