package pl.edu.pw.ddm.platform.runner;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.stream.Collectors;

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
import pl.edu.pw.ddm.platform.runner.utils.MethodPersister;

class LocalExecutionRunner implements FlatMapFunction<Iterator<Integer>, ModelWrapper> {

    private final String dataId;

    LocalExecutionRunner(String dataId) {
        this.dataId = dataId;
    }

    @Override
    public Iterator<ModelWrapper> call(Iterator<Integer> iterator) throws Exception {
        MiningMethod method = MethodPersister.load();
        StringModel model = new StringModel(perform(method));

        ModelWrapper wrapper = ModelWrapper.local(model, InetAddress.getLocalHost().toString(), null);
        return new SingletonIterator(wrapper);
    }

    private String perform(MiningMethod method) {
        if (method instanceof Classifier) {
            return classify((Classifier) method);
        } else if (method instanceof Clustering) {
            return cluster((Clustering) method);
        } else {
            throw new IllegalArgumentException("Unknown mining method.");
        }
    }

    private String classify(Classifier classifier) {
        NodeDataProvider dataProvider = new NodeDataProvider(dataId);
        SampleProvider sampleProvider = NodeSampleProvider.fromData(dataProvider.test());
        NodeResultCollector resultCollector = new NodeResultCollector();
        ParamProvider paramProvider = new NodeParamProvider();

        classifier.classify(sampleProvider, paramProvider, resultCollector);
        resultCollector.saveResults();

        String resultStr = resultCollector.getResults()
                .stream()
                .map(NodeResultCollector.NodeResultData::toString)
                .collect(Collectors.joining(", "));
        return "classified: " + resultStr;
    }

    private String cluster(Clustering clustering) {
        NodeDataProvider dataProvider = new NodeDataProvider(dataId);
        NodeResultCollector resultCollector = new NodeResultCollector();
        ParamProvider paramProvider = new NodeParamProvider();

        clustering.cluster(dataProvider, paramProvider, resultCollector);
        resultCollector.saveResults();

        String resultStr = resultCollector.getResults()
                .stream()
                .map(NodeResultCollector.NodeResultData::toString)
                .collect(Collectors.joining(", "));
        return "clustered: " + resultStr;
    }

}
