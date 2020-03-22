package pl.edu.pw.ddm.platform.runner;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.spark.api.java.function.FlatMapFunction;
import pl.edu.pw.ddm.platform.interfaces.data.ResultCollector;
import pl.edu.pw.ddm.platform.interfaces.data.SampleData;
import pl.edu.pw.ddm.platform.interfaces.data.SampleProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.Classifier;
import pl.edu.pw.ddm.platform.interfaces.mining.Clustering;
import pl.edu.pw.ddm.platform.interfaces.mining.MiningMethod;
import pl.edu.pw.ddm.platform.runner.models.ModelWrapper;
import pl.edu.pw.ddm.platform.runner.models.StringModel;
import pl.edu.pw.ddm.platform.runner.utils.MethodPersister;

class LocalExecutionRunner implements FlatMapFunction<Iterator<Integer>, ModelWrapper> {

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
        Sampler sampleProvider = new Sampler();
        IntStream.range(0, 40)
                .forEach(i -> sampleProvider.addSample(i + ".", i));
        Resulter resultCollector = new Resulter();

        classifier.classify(sampleProvider, resultCollector);

        String resultStr = String.join(", ", resultCollector.getResults());
        return "classified: " + resultStr;
    }

    private String cluster(Clustering clustering) {
        return "clustered";
    }

    @Data
    static class Resulter implements ResultCollector {

        private List<String> results = new ArrayList<>();

        @Override
        public void collect(String id, String result) {
            results.add("[" + id + "][String]: " + result);
        }

        @Override
        public void collect(String id, double result) {
            results.add("[" + id + "][Double]: " + result);

        }
    }

    @Data
    static class Sampler implements SampleProvider {

        private List<SampleData> samples = new ArrayList<>();
        private Iterator<SampleData> iterator = null;

        void addSample(String id, double value) {
            samples.add(new SamplerData(id, null, value));
        }

        @Override
        public boolean hasNext() {
            if (iterator == null) {
                iterator = samples.iterator();
            }
            return iterator.hasNext();
        }

        @Override
        public SampleData next() {
            if (iterator == null) {
                iterator = samples.iterator();
            }
            return iterator.next();
        }
    }

    @AllArgsConstructor
    static class SamplerData implements SampleData {

        private String id;
        private String label;
        private double data;

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public String[] getAttributes() {
            return new String[0];
        }

        @Override
        public double[] getNumericAttributes() {
            return new double[0];
        }

        @Override
        public String getAttribute(int i) {
            return null;
        }

        @Override
        public String getAttribute(String s) {
            return null;
        }

        @Override
        public double getNumericAttribute(int i) {
            return data;
        }

        @Override
        public double getNumericAttribute(String s) {
            return 0;
        }
    }

}
