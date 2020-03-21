package pl.edu.pw.ddm.platform.runner;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.spark.api.java.function.FlatMapFunction;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

class GlobalProcessRunner implements FlatMapFunction<Iterator<LocalModel>, ModelWrapper> {

    @Override
    public Iterator<ModelWrapper> call(Iterator<LocalModel> iterator) throws Exception {
        List<LocalModel> models = new ArrayList<>();
        while (iterator.hasNext()) {
            models.add(iterator.next());
        }

        GlobalModel globalModel = AlgorithmInitializer.initGlobalProcessor()
                .processGlobal(models, null);

        ModelWrapper wrapper = ModelWrapper.global(globalModel, InetAddress.getLocalHost().toString());
        return new SingletonIterator(wrapper);
    }

}
