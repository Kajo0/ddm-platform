package ddm.sample

import pl.edu.pw.ddm.platform.interfaces.data.*
import spock.lang.Specification

class RandomClassifierSpec extends Specification {

    DataProvider dataProvider
    ParamProvider paramProvider
    ResultCollector resultCollector

    def setup() {
        dataProvider = Stub(DataProvider)
        paramProvider = Stub(ParamProvider)
        resultCollector = Mock(ResultCollector)
    }

    def "should classify 3 nodes"() {
        setup: "processors"
        def localProcessors = (1..3).collect { new RandomClassifier() }
        def globalProcessor = new RandomClassifier()
        def localUpdateProcessors = (1..3).collect { new RandomClassifier() }

        and: "data"
        dataProvider.training() >>> [
                [data(1, 'A'), data(11, 'A'), data(111, 'A'), data(1111, 'B'), data(11111, 'B')],
                [data(2, 'B'), data(22, 'B'), data(222, 'B'), data(2222, 'C'), data(22222, 'C')],
                [data(3, 'C')]
        ]

        when: "process local"
        def localModels = localProcessors.collect { it.processLocal(dataProvider, paramProvider) }

        then:
        localModels[0].localLabels == ['A', 'B'] as Set
        localModels[1].localLabels == ['B', 'C'] as Set
        localModels[2].localLabels == ['C'] as Set

        when: "process global"
        def globalModel = globalProcessor.processGlobal(localModels, paramProvider)

        then:
        globalModel.allLabels == ['A', 'B', 'C'] as Set

        when: "update local"
        def localMethods = localUpdateProcessors.withIndex()
                .collect { RandomClassifier processor, int index -> processor.updateLocal(localModels[index], globalModel, dataProvider, paramProvider) }

        then: "local methods has own model values"
        localMethods[0].labelPercent == 66
        localMethods[1].labelPercent == 66
        localMethods[2].labelPercent == 33

        when: "classification on 1. node is done"
        localMethods[0].classify(createSampleProvider(), paramProvider, resultCollector)

        then: "next rands for seed 66 provide labels from set as follows [B, A, B, B, A]"
        1 * resultCollector.collect('1', 'B')
        1 * resultCollector.collect('2', 'A')
        1 * resultCollector.collect('3', 'B')
        1 * resultCollector.collect('4', 'B')
        1 * resultCollector.collect('5', 'A')

        when: "classification on 2. node is done"
        localMethods[1].classify(createSampleProvider(), paramProvider, resultCollector)

        then: "next rands for seed 66 provide labels from set as follows [B, A, B, B, A]"
        1 * resultCollector.collect('1', 'B')
        1 * resultCollector.collect('2', 'A')
        1 * resultCollector.collect('3', 'B')
        1 * resultCollector.collect('4', 'B')
        1 * resultCollector.collect('5', 'A')

        when: "classification on 3. node is done"
        localMethods[2].classify(createSampleProvider(), paramProvider, resultCollector)

        then: "next rands for seed 33 provide labels from set as follows [A, A, C, C, B]"
        1 * resultCollector.collect('1', 'A')
        1 * resultCollector.collect('2', 'A')
        1 * resultCollector.collect('3', 'C')
        1 * resultCollector.collect('4', 'C')
        1 * resultCollector.collect('5', 'B')
    }

    def data(def id, def label) {
        Stub(Data) {
            getId() >> id
            getLabel() >> label
        }
    }

    def sample(def id, def label) {
        Stub(SampleData) {
            getId() >> id
            getLabel() >> label
        }
    }

    def createSampleProvider() {
        def samples = [
                sample('1', 'A'),
                sample('2', 'A'),
                sample('3', 'A'),
                sample('4', 'A'),
                sample('5', 'A')
        ]
        new SampleProviderImpl(samples: samples.iterator())
    }

    class SampleProviderImpl implements SampleProvider {

        Iterator<SampleData> samples

        @Override
        boolean hasNext() {
            return samples.hasNext()
        }

        @Override
        SampleData next() {
            return samples.next()
        }

        @Override
        Collection<SampleData> all() {
            return samples
        }
    }

}
