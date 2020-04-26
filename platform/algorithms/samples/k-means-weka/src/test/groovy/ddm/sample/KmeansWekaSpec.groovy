package ddm.sample

import pl.edu.pw.ddm.platform.interfaces.data.*
import spock.lang.Specification

class KmeansWekaSpec extends Specification {

    DataProvider dataProvider
    ParamProvider paramProvider

    def setup() {
        dataProvider = Stub(DataProvider)
        paramProvider = Stub(ParamProvider)
    }

    def "should perform clustering 2 groups in clustering stage"() {
        setup:
        def algorithm = new KmeansWeka()

        and:
        paramProvider.provideNumeric('groups') >> 2
        paramProvider.provideNumeric('iterations') >> 10
        def sampleProvider = new SampleProviderImpl([
                sample(0, 'A', [0, 0, 0, 0]),
                sample(1, 'A', [0, 1, 0, 0]),
                sample(2, 'A', [1, 0, 0, 0]),
                sample(3, 'A', [1, 1, 0, 0]),

                sample(4, 'B', [10, 10, 0, 0]),
                sample(5, 'B', [10, 10, 0, 1]),
                sample(6, 'B', [10, 10, 1, 0]),
                sample(7, 'B', [10, 10, 1, 1]),
        ])
        def resultCollector = new ResultCollectorImpl()

        when: "builds dummy clustering alg"
        def kmeans = algorithm.updateLocal(null, null, dataProvider, paramProvider)

        then: "centroids are not calculated"
        (kmeans as Clusterer).centroids == null

        when: "perform clustering"
        kmeans.cluster(sampleProvider, paramProvider, resultCollector)

        then: "found two clusters"
        resultCollector.results['0'] == resultCollector.results['1']
        resultCollector.results['1'] == resultCollector.results['2']
        resultCollector.results['2'] == resultCollector.results['3']

        resultCollector.results['4'] == resultCollector.results['5']
        resultCollector.results['5'] == resultCollector.results['6']
        resultCollector.results['6'] == resultCollector.results['7']
    }

    def "should perform clustering 2 groups in update local stage"() {
        setup:
        def algorithm = new KmeansWeka()
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
        paramProvider.provide('preCalcCentroids') >> true
        paramProvider.provideNumeric('groups') >> 2
        paramProvider.provideNumeric('iterations') >> 10
        def dataProvider = new DataProviderImpl(data: data)
        def sampleProvider = new SampleProviderImpl(data)
        def resultCollector = new ResultCollectorImpl()

        when: "builds dummy clustering alg"
        def kmeans = algorithm.updateLocal(null, null, dataProvider, paramProvider)

        then: "centroids are calculated"
        def centroids = (kmeans as Clusterer).centroids
        centroids != null
        centroids.size() == 2
        centroids[0].toDoubleArray() == [10, 10, 0.5, 0.5] || centroids[0].toDoubleArray() == [0.5, 0.5, 0, 0]
        centroids[1].toDoubleArray() == [0.5, 0.5, 0, 0] || centroids[1].toDoubleArray() == [10, 10, 0.5, 0.5]

        when: "perform clustering"
        kmeans.cluster(sampleProvider, paramProvider, resultCollector)

        then: "found two clusters"
        resultCollector.results['0'] == resultCollector.results['1']
        resultCollector.results['1'] == resultCollector.results['2']
        resultCollector.results['2'] == resultCollector.results['3']

        resultCollector.results['4'] == resultCollector.results['5']
        resultCollector.results['5'] == resultCollector.results['6']
        resultCollector.results['6'] == resultCollector.results['7']
    }

    def sample(def id, def label, def attrs) {
        Stub(SampleData) {
            getId() >> id
            getNumericAttributes() >> attrs
            getLabel() >> label
        }
    }

    class DataProviderImpl implements DataProvider {

        Collection<Data> data

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
