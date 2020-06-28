package pl.edu.pw.ddm.platform.testing.interfaces.impl

import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.impl.AoptkmDDM
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider
import pl.edu.pw.ddm.platform.metrics.ClusteringMetrics
import pl.edu.pw.ddm.platform.metrics.dto.IdLabel
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.NodeDataProvider
import spock.lang.Specification

class DdmPipelineRunnerSpec extends Specification {

    def "should run DDM pipeline runner"() {
        given:
        def aoptkm = new AoptkmDDM()
        def trainDataPath = getClass().getResource('/iris.train').path

        and:
        def config = DdmExecutionConfig.builder()
                .algorithmConfig(aoptkm)
                .miningMethod(aoptkm)
                .dataPath([trainDataPath])
                .testDataPath(getClass().getResource('/iris.test').path)
                .separator(',')
                .idIndex(0)
                .labelIndex(5)
                .attributesAmount(4)
                .colTypes(['numeric', 'numeric', 'numeric', 'numeric', 'numeric', 'nominal'] as String[])
                .distanceFunction(null)
                .executionParams([
                        'groups'      : '3',
                        'iterations'  : '11',
                        'noOneGroup'  : 'true',
                        'minKGroups'  : 'true',
                        'exactKGroups': 'false',
                ])
                .build()
        def cr = new DdmPipelineRunner(config)

        and:
        def desc = DataProvider.DataDesc.builder()
                .separator(',')
                .idIndex(0)
                .labelIndex(5)
                .attributesAmount(4)
                .colTypes(['numeric', 'numeric', 'numeric', 'numeric', 'numeric', 'nominal'] as String[])
                .build()
        def dp = new NodeDataProvider(trainDataPath, null, desc, false)
        def labels = dp.training()
                .collect { IdLabel.of(it.id, it.label) }

        when:
        def results = cr.run()

        then:
        results.results.size() == 78

        and:
        def predictions = results.results
                .collect { IdLabel.of(it.id, it.value) }

        def metrics = new ClusteringMetrics(predictions, labels)
        def ari = metrics.adjustedRandIndex()
        ari >= 0
        ari <= 1

        and:
        def finalGroups = predictions.collect { it.label }
                .unique()
                .size()
        println "Final groups: $finalGroups"
        println "ARI: $ari"
    }

}
