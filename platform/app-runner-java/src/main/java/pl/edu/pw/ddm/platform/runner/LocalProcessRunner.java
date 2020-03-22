package pl.edu.pw.ddm.platform.runner;

import java.net.InetAddress;
import java.util.Iterator;

import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.spark.api.java.function.FlatMapFunction;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;
import pl.edu.pw.ddm.platform.runner.models.ModelWrapper;
import pl.edu.pw.ddm.platform.runner.utils.AlgorithmProcessorInitializer;
import pl.edu.pw.ddm.platform.runner.utils.ModelPersister;
import pl.edu.pw.ddm.platform.runner.utils.PersistentIdStamper;

class LocalProcessRunner implements FlatMapFunction<Iterator<Integer>, ModelWrapper> {

    @Override
    public Iterator<ModelWrapper> call(Iterator<Integer> iterator) throws Exception {
        Integer id = iterator.next();
        PersistentIdStamper.save(id);

        LocalModel model = AlgorithmProcessorInitializer.initLocalProcessor()
                .processLocal(null, null);
        ModelPersister.saveLocal(model);

        ModelWrapper wrapper = ModelWrapper.local(model, InetAddress.getLocalHost().toString(), id);
        return new SingletonIterator(wrapper);
    }

}