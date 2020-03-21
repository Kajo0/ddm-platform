package pl.edu.pw.ddm.platform.runner;

import java.net.InetAddress;
import java.util.Iterator;

import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.spark.api.java.function.FlatMapFunction;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;
import pl.edu.pw.ddm.platform.runner.models.ModelWrapper;
import pl.edu.pw.ddm.platform.runner.models.StringLocalModel;
import pl.edu.pw.ddm.platform.runner.utils.PersistentIdStamper;

class LocalUpdateRunner implements FlatMapFunction<Iterator<GlobalModel>, ModelWrapper> {

    @Override
    public Iterator<ModelWrapper> call(Iterator<GlobalModel> iterator) throws Exception {
        Integer id = PersistentIdStamper.read();
//        LocalModel previousModel = null;
//        MiningMethod method = AlgorithmInitializer.initLocalProcessor()
//                .updateLocal(previousModel, iterator.next(), null, null);

        LocalModel model = new StringLocalModel("time=" + System.currentTimeMillis());
        ModelWrapper wrapper = ModelWrapper.local(model, InetAddress.getLocalHost().toString(), id);
        return new SingletonIterator(wrapper);
    }

}
