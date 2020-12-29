package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2

import pl.edu.pw.ddm.platform.distfunc.EuclideanDistance
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider
import pl.edu.pw.ddm.platform.metrics.ClassificationMetrics
import pl.edu.pw.ddm.platform.metrics.dto.IdLabel
import pl.edu.pw.ddm.platform.testing.interfaces.impl.DdmExecutionConfig
import pl.edu.pw.ddm.platform.testing.interfaces.impl.DdmPipelineRunner
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.NodeDataProvider
import spock.lang.Specification

class DMeb2TestingLocalClassifierOnlySpec extends Specification {

    /**
     * Used rather as debug not test cause of collecting localhost address which here is the same LocalRepresentativesModel()#dummyObservation
     */
    def "should perform correct classification of iris training data using only local classifiers"() {
        given:
        def pipeline = new DMeb2()
        def miningMethod = new GlobalClassifier(null, null, null)

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
                        'seed'                : '12',
                        'meb_clusters'        : '20',
                        'kernel'              : 'linear',
                        'knn_k'               : '3',
                        'use_local_classifier': 'true'
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

}
