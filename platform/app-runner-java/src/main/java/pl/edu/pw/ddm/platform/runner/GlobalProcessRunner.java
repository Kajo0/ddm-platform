package pl.edu.pw.ddm.platform.runner;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.spark.api.java.function.FlatMapFunction;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;
import pl.edu.pw.ddm.platform.runner.data.NodeParamProvider;
import pl.edu.pw.ddm.platform.runner.models.ModelWrapper;
import pl.edu.pw.ddm.platform.runner.utils.AlgorithmProcessorInitializer;

class GlobalProcessRunner implements FlatMapFunction<Iterator<LocalModel>, ModelWrapper> {

    @Override
    public Iterator<ModelWrapper> call(Iterator<LocalModel> iterator) throws Exception {
        List<LocalModel> models = new ArrayList<>();
        while (iterator.hasNext()) {
            models.add(iterator.next());
        }

        // TODO move to runner class and pass as clojure
        ParamProvider paramProvider = new NodeParamProvider();

        GlobalModel globalModel = AlgorithmProcessorInitializer.initGlobalProcessor()
                .processGlobal(models, paramProvider);

        ModelWrapper wrapper = ModelWrapper.global(globalModel, InetAddress.getLocalHost().toString());
        return new SingletonIterator(wrapper);
    }

}
