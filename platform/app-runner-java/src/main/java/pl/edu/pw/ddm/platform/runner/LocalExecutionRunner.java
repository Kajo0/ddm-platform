package pl.edu.pw.ddm.platform.runner;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Iterator;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.spark.api.java.function.FlatMapFunction;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.data.SampleProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.Classifier;
import pl.edu.pw.ddm.platform.interfaces.mining.Clustering;
import pl.edu.pw.ddm.platform.interfaces.mining.MiningMethod;
import pl.edu.pw.ddm.platform.runner.data.NodeDataProvider;
import pl.edu.pw.ddm.platform.runner.data.NodeParamProvider;
import pl.edu.pw.ddm.platform.runner.data.NodeResultCollector;
import pl.edu.pw.ddm.platform.runner.data.NodeSampleProvider;
import pl.edu.pw.ddm.platform.runner.models.ModelWrapper;
import pl.edu.pw.ddm.platform.runner.models.StringModel;
import pl.edu.pw.ddm.platform.runner.models.TimeStatistics;
import pl.edu.pw.ddm.platform.runner.utils.MethodPersister;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
class LocalExecutionRunner implements FlatMapFunction<Iterator<Integer>, ModelWrapper> {

    private final InitParamsDto initParams;

    @Override
    public Iterator<ModelWrapper> call(Iterator<Integer> iterator) throws Exception {
        MiningMethod method = MethodPersister.load(initParams.getExecutionPath(), initParams.getExecutionId());
        StringModel model = new StringModel(method.type() + " ok");

        TimeStatistics stats = perform(method);
        ModelWrapper wrapper = ModelWrapper.local(model, InetAddress.getLocalHost().toString(), null);
        wrapper.getTimeStatistics().setStart(stats.getStart());
        wrapper.getTimeStatistics().setEnd(stats.getEnd());
        wrapper.getTimeStatistics().setDataLoadingMillis(stats.getDataLoadingMillis());

        return new SingletonIterator(wrapper);
    }

    private TimeStatistics perform(MiningMethod method) {
        if (method instanceof Classifier) {
            return classify((Classifier) method);
        } else if (method instanceof Clustering) {
            return cluster((Clustering) method);
        } else {
            throw new IllegalArgumentException("Unknown mining method.");
        }
    }

    private TimeStatistics classify(Classifier classifier) {
        NodeDataProvider dataProvider = new NodeDataProvider(initParams.getDatasetsPath(), initParams.getTrainDataId(), initParams.getTestDataId());
        SampleProvider sampleProvider = NodeSampleProvider.fromData(dataProvider.test());
        NodeResultCollector resultCollector = new NodeResultCollector(initParams.getExecutionPath(), initParams.getExecutionId());
        ParamProvider paramProvider = new NodeParamProvider(initParams.findDistanceFunction(), initParams.getExecutionParams());

        TimeStatistics stats = new TimeStatistics();
        stats.setStart(LocalDateTime.now());
        classifier.classify(sampleProvider, paramProvider, resultCollector);
        stats.setEnd(LocalDateTime.now());
        stats.setDataLoadingMillis(dataProvider.getLoadingMillis());

        resultCollector.saveResults();
        return stats;
    }

    private TimeStatistics cluster(Clustering clustering) {
        NodeDataProvider dataProvider = new NodeDataProvider(initParams.getDatasetsPath(), initParams.getTrainDataId(), initParams.getTestDataId());
        SampleProvider sampleProvider = NodeSampleProvider.fromData(dataProvider.training()); // TODO think if not test for clustering means eg training
        NodeResultCollector resultCollector = new NodeResultCollector(initParams.getExecutionPath(), initParams.getExecutionId());
        ParamProvider paramProvider = new NodeParamProvider(initParams.findDistanceFunction(), initParams.getExecutionParams());

        TimeStatistics stats = new TimeStatistics();
        stats.setStart(LocalDateTime.now());
        clustering.cluster(sampleProvider, paramProvider, resultCollector);
        stats.setEnd(LocalDateTime.now());
        stats.setDataLoadingMillis(dataProvider.getLoadingMillis());

        resultCollector.saveResults();
        return stats;
    }

}
