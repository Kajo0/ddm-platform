package pl.edu.pw.ddm.platform.runner;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Iterator;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.spark.api.java.function.FlatMapFunction;
import pl.edu.pw.ddm.platform.interfaces.mining.MiningMethod;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;
import pl.edu.pw.ddm.platform.runner.models.ModelWrapper;
import pl.edu.pw.ddm.platform.runner.models.StringLocalModel;
import pl.edu.pw.ddm.platform.runner.utils.MethodPersister;
import pl.edu.pw.ddm.platform.runner.utils.PersistentIdStamper;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
class LocalFinalMethodCollector implements FlatMapFunction<Iterator<MiningMethod>, ModelWrapper> {

    private final InitParamsDto initParams;

    @Override
    public Iterator<ModelWrapper> call(Iterator<MiningMethod> iterator) throws Exception {
        Integer id = PersistentIdStamper.read();

        MiningMethod method = iterator.next();
        MethodPersister.save(initParams.getExecutionPath(), method, initParams.getExecutionId());

        LocalModel model = new StringLocalModel("ackTime=" + System.currentTimeMillis());
        ModelWrapper wrapper = ModelWrapper.local(model, InetAddress.getLocalHost().toString(), id);
        wrapper.getTimeStatistics().setStart(LocalDateTime.now());
        wrapper.getTimeStatistics().setEnd(LocalDateTime.now());
        wrapper.getTimeStatistics().setDataLoadingMillis(0);

        return new SingletonIterator(wrapper);
    }

}
