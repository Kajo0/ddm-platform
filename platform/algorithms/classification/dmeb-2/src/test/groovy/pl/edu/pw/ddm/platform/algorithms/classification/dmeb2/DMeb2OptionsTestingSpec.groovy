package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2

import pl.edu.pw.ddm.platform.distfunc.EuclideanDistance
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider
import pl.edu.pw.ddm.platform.metrics.ClassificationMetrics
import pl.edu.pw.ddm.platform.metrics.dto.IdLabel
import pl.edu.pw.ddm.platform.testing.interfaces.impl.DdmExecutionConfig
import pl.edu.pw.ddm.platform.testing.interfaces.impl.DdmPipelineRunner
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.NodeDataProvider
import spock.lang.Specification

class DMeb2OptionsTestingSpec extends Specification {

    def "should perform correct classifications of separated training data"() {
        given:
        def pipeline = new DMeb2()
        def miningMethod = new GlobalClassifier(null, null, null)

        and:
        def config = DdmExecutionConfig.builder()
                .algorithmConfig(pipeline)
                .miningMethod(miningMethod)
                .dataPath([
                        getClass().getResource('/separated_1.data').path,
                        getClass().getResource('/separated_2.data').path,
                        getClass().getResource('/separated_3.data').path
                ])
                .testDataPath(getClass().getResource('/separated_0.data').path)
                .separator('\t')
                .idIndex(0)
                .labelIndex(3)
                .attributesAmount(2)
                .colTypes(null) // null to use deduct
                .distanceFunction(new EuclideanDistance())
                .executionParams([
                        'seed'                                    : '10',
                        'meb_clusters'                            : '-1',
                        'kernel'                                  : 'linear',
                        'knn_k'                                   : '5',
                        'init_kmeans_method'                      : 'k-means++',
                        'use_local_classifier'                    : 'false',
                        'use_first_level_only'                    : 'false',
                        'local_method_for_svs_clusters'           : 'close_to',
                        'close_to_percent'                        : '0.2',
                        'local_method_for_non_multiclass_clusters': 'metrics_collect',
                        'random_percent'                          : '0.1',
                        'global_expand_percent'                   : '0.2',
                        'debug'                                   : 'true'
                ])
                .build()
        def cr = new DdmPipelineRunner(config)

        when:
        def results = cr.run()

        then:
        results.results.size()

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
