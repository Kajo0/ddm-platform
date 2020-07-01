package pl.edu.pw.ddm.platform.algorithms.classification.dmeb

import pl.edu.pw.ddm.platform.distfunc.EuclideanDistance
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider
import pl.edu.pw.ddm.platform.metrics.ClassificationMetrics
import pl.edu.pw.ddm.platform.metrics.dto.IdLabel
import pl.edu.pw.ddm.platform.testing.interfaces.impl.DdmExecutionConfig
import pl.edu.pw.ddm.platform.testing.interfaces.impl.DdmPipelineRunner
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.NodeDataProvider
import spock.lang.Specification

class DMebTestingSpec extends Specification {

    def "should perform correct clustering of iris training data"() {
        given:
        def pipeline = new DMeb()
        def miningMethod = new DMeb()

        and:
        def config = DdmExecutionConfig.builder()
                .algorithmConfig(pipeline)
                .miningMethod(miningMethod)
                .dataPath([
                        getClass().getResource('/iris_1.train').path,
                        getClass().getResource('/iris_2.train').path
                ])
                .testDataPath(getClass().getResource('/iris.test').path)
                .separator(',')
                .idIndex(0)
                .labelIndex(5)
                .attributesAmount(4)
                .colTypes(null) // null to use deduct
                .distanceFunction(new EuclideanDistance())
                .executionParams([
                        'seed'        : '12',
                        'meb_clusters': '20',
                        'kernel'      : 'linear'
                ])
                .build()
        def cr = new DdmPipelineRunner(config)

        when:
        def results = cr.run()

        then:
        results.results.size() == 72

        and:
        def desc = DataProvider.DataDesc.builder()
                .separator(config.separator)
                .idIndex(config.idIndex)
                .labelIndex(config.labelIndex)
                .attributesAmount(config.attributesAmount)
                .colTypes(config.colTypes)
                .build()
        def labels = new NodeDataProvider(
                config.testDataPath,
                config.testDataPath,
                desc,
                true
        )
                .test()
                .collect { IdLabel.of(it.id, it.label) }

        and:
        def predictions = results.results
                .collect { IdLabel.of(it.id, it.value) }

        def metrics = new ClassificationMetrics(predictions, labels)
        def acc = metrics.accuracy()
        def fm = metrics.fMeasure()

        and: "print results"
        println("Accuracy: $acc")
        println("F-measure: $fm")
    }

    def "should perform correct clustering of R15 training data"() {
        given:
        def pipeline = new DMeb()
        def miningMethod = new DMeb()

        and:
        def config = DdmExecutionConfig.builder()
                .algorithmConfig(pipeline)
                .miningMethod(miningMethod)
                .dataPath([
                        getClass().getResource('/R15_node_1.data').path,
                        getClass().getResource('/R15_node_2.data').path,
                        getClass().getResource('/R15_node_3.data').path
                ])
                .testDataPath(getClass().getResource('/R15_test.data').path)
                .separator('\t')
                .idIndex(0)
                .labelIndex(3)
                .attributesAmount(2)
                .colTypes(null) // null to use deduct
                .distanceFunction(new EuclideanDistance())
                .executionParams([
                        'seed'        : '10',
                        'meb_clusters': '2',
                        'kernel'      : 'rbf'
                ])
                .build()
        def cr = new DdmPipelineRunner(config)

        when:
        def results = cr.run()

        then:
        results.results.size() == 200

        and:
        def desc = DataProvider.DataDesc.builder()
                .separator(config.separator)
                .idIndex(config.idIndex)
                .labelIndex(config.labelIndex)
                .attributesAmount(config.attributesAmount)
                .colTypes(config.colTypes)
                .build()
        def labels = new NodeDataProvider(
                config.testDataPath,
                config.testDataPath,
                desc,
                true
        )
                .test()
                .collect { IdLabel.of(it.id, it.label) }

        and:
        def predictions = results.results
                .collect { IdLabel.of(it.id, it.value) }

        def metrics = new ClassificationMetrics(predictions, labels)
        def acc = metrics.accuracy()
        def fm = metrics.fMeasure()

        and: "print results"
        println("Accuracy: $acc")
        println("F-measure: $fm")
    }

}
