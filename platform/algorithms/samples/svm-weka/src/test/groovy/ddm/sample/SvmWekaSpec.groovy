package ddm.sample

import pl.edu.pw.ddm.platform.interfaces.data.*
import spock.lang.Specification

class SvmWekaSpec extends Specification {

    ParamProvider paramProvider

    def setup() {
        paramProvider = Stub(ParamProvider)
    }

    def "should perform correct classification of training data"() {
        setup:
        def algorithm = new SvmWeka()
        def data = [
                sample(0, 'A', [0, 0, 0, 0]),
                sample(1, 'A', [0, 1, 0, 0]),
                sample(2, 'A', [1, 0, 0, 0]),
                sample(3, 'A', [1, 1, 0, 0]),

                sample(4, 'B', [10, 10, 0, 0]),
                sample(5, 'B', [10, 10, 0, 1]),
                sample(6, 'B', [10, 10, 1, 0]),
                sample(7, 'B', [10, 10, 1, 1]),
        ]

        and:
        paramProvider.provide('options', _ as String) >> '-C 12.5 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K "weka.classifiers.functions.supportVector.RBFKernel -C 250007 -G 0.50625"'
        def dataProvider = new DataProviderImpl(data: data)
        def sampleProvider = new SampleProviderImpl(data)
        def resultCollector = new ResultCollectorImpl()

        when: "builds classifier and performs classifier"
        def svm = algorithm.updateLocal(null, null, dataProvider, paramProvider)

        and: "performs classification"
        svm.classify(sampleProvider, paramProvider, resultCollector)

        then: "classification is done"
        resultCollector.results['0'] == 'A'
        resultCollector.results['1'] == 'A'
        resultCollector.results['2'] == 'A'
        resultCollector.results['3'] == 'A'

        resultCollector.results['4'] == 'B'
        resultCollector.results['5'] == 'B'
        resultCollector.results['6'] == 'B'
        resultCollector.results['7'] == 'B'
    }

    def "should perform correct classification of test data"() {
        setup:
        def algorithm = new SvmWeka()

        and:
        paramProvider.provide('options', _ as String) >> '-C 12.5 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K "weka.classifiers.functions.supportVector.RBFKernel -C 250007 -G 0.50625"'
        def dataProvider = new DataProviderImpl(data: [
                sample(0, 'B', [0, 0, 0, 0]),
                sample(1, 'B', [0, 1, 0, 0]),
                sample(2, 'B', [1, 0, 0, 0]),

                sample(4, 'A', [10, 10, 10, 10]),
                sample(5, 'A', [10, 10, 10, 11]),
                sample(6, 'A', [10, 10, 11, 10]),
        ])
        def sampleProvider = new SampleProviderImpl([
                sample(null, null, [0, 7, 0, 0]),
                sample(null, null, [5, 1, 2, 1]),

                sample(null, null, [10, 10, 5, 7]),
                sample(null, null, [7, 8, 1, 5]),
        ])
        def resultCollector = new ResultCollectorImpl()

        when: "builds classifier and performs classifier"
        def svm = algorithm.updateLocal(null, null, dataProvider, paramProvider)

        and: "performs classification"
        svm.classify(sampleProvider, paramProvider, resultCollector)

        then: "classification is done"
        resultCollector.results['0'] == 'B'
        resultCollector.results['1'] == 'B'

        resultCollector.results['2'] == 'A'
        resultCollector.results['3'] == 'A'
    }

    def sample(def id, def label, def attrs) {
        new SampleDataImpl(id: id, attributes: attrs, label: label)
    }

    class SampleDataImpl implements SampleData {

        String id
        List<Double> attributes
        String label

        @Override
        String getId() {
            return id
        }

        @Override
        String getLabel() {
            return label
        }

        @Override
        String[] getAttributes() {
            throw new RuntimeException("not implemented")
        }

        @Override
        double[] getNumericAttributes() {
            return attributes
        }

        @Override
        String getAttribute(int i) {
            throw new RuntimeException("not implemented")
        }

        @Override
        String getAttribute(String s) {
            throw new RuntimeException("not implemented")
        }

        @Override
        double getNumericAttribute(int i) {
            return attributes[i]
        }

        @Override
        double getNumericAttribute(String s) {
            throw new RuntimeException("not implemented")
        }
    }

    class DataProviderImpl implements DataProvider {

        Collection<Data> data

        @Override
        DataDesc getDataDescription() {
            return null
        }

        @Override
        Collection<Data> training() {
            return data
        }

        @Override
        Collection<Data> test() {
            return data
        }

        @Override
        Collection<Data> all() {
            return data
        }
    }

    class SampleProviderImpl implements SampleProvider {

        Collection<SampleData> samples
        Iterator<SampleData> iterator

        SampleProviderImpl(def samples) {
            this.samples = samples
            this.iterator = samples.iterator()
        }

        @Override
        boolean hasNext() {
            return iterator.hasNext()
        }

        @Override
        SampleData next() {
            return iterator.next()
        }

        @Override
        Collection<SampleData> all() {
            return samples
        }
    }

    class ResultCollectorImpl implements ResultCollector {

        Map<String, String> results = [:]

        @Override
        void collect(String id, String result) {
            results[id] = result
        }

        @Override
        void collect(String id, double result) {
            results[id] = result as String
        }
    }

}
