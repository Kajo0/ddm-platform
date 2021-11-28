package pl.edu.pw.ddm.platform.algorithms.clustering.dbirch

import pl.edu.pw.ddm.platform.distfunc.EuclideanDistance
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider
import pl.edu.pw.ddm.platform.metrics.ClusteringMetrics
import pl.edu.pw.ddm.platform.metrics.dto.IdLabel
import pl.edu.pw.ddm.platform.testing.interfaces.impl.DdmExecutionConfig
import pl.edu.pw.ddm.platform.testing.interfaces.impl.DdmPipelineRunner
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.NodeDataProvider
import spock.lang.Specification

class DBirchTestingSpec extends Specification {

    def "should perform correct clustering of iris training data"() {
        given:
        def config = DdmExecutionConfig.builder()
                .algorithmConfig(new DBirchConfig())
                .miningMethod(new DBirchClusterer())
                .dataPath([
                        getClass().getResource('/iris.train').path,
                        getClass().getResource('/iris.test').path
                ])
                .testDataPath(getClass().getResource('/data_dummy.test').path)
                .separator(',')
                .idIndex(0)
                .labelIndex(5)
                .attributesAmount(4)
                .colTypes(null) // null to use deduct
                .distanceFunction(new EuclideanDistance())
                .executionParams([
                        'groups'          : '3',
                        'branching_factor': '50',
                        'threshold'       : '0.5',
                        'g_groups'        : '3',
                        'g_threshold'     : '0.5'
                ])
                .build()
        def cr = new DdmPipelineRunner(config)

        when:
        def results = cr.run()

        then:
        results.results.size() == 150

        and:
        def desc = DataProvider.DataDesc.builder()
                .separator(config.separator)
                .idIndex(config.idIndex)
                .labelIndex(config.labelIndex)
                .attributesAmount(config.attributesAmount)
                .colTypes(config.colTypes)
                .build()
        def labels = config.dataPath
                .collectMany { path ->
                    new NodeDataProvider(
                            path,
                            config.testDataPath,
                            desc,
                            true
                    ).training()
                }.collect { IdLabel.of(it.id, it.label) }

        and:
        def predictions = results.results
                .collect { IdLabel.of(it.id, it.value) }

        def metrics = new ClusteringMetrics(predictions, labels)
        def ari = metrics.adjustedRandIndex()
        ari >= 0
        ari <= 1

        and: "print results"
        println("ARI: $ari")

        and:
        def groups = results.results
                .collect { it.value }
                .unique()
        println("${groups.size()} groups: $groups")
    }

    def "should perform correct clustering of R15 training data"() {
        given:
        def config = DdmExecutionConfig.builder()
                .algorithmConfig(new DBirchConfig())
                .miningMethod(new DBirchClusterer())
                .dataPath([
                        getClass().getResource('/R15_node_1.data').path,
                        getClass().getResource('/R15_node_2.data').path,
                        getClass().getResource('/R15_node_3.data').path
                ])
                .testDataPath(getClass().getResource('/data_dummy.test').path)
                .separator('\t')
                .idIndex(0)
                .labelIndex(3)
                .attributesAmount(2)
                .colTypes(null) // null to use deduct
                .distanceFunction(new EuclideanDistance())
                .executionParams([
                        'groups'          : '15',
                        'branching_factor': '50',
                        'threshold'       : '0.5',
                        'g_groups'        : '15',
                        'g_threshold'     : '0.005'
                ])
                .build()
        def cr = new DdmPipelineRunner(config)

        when:
        def results = cr.run()

        then:
        results.results.size() == 600

        and:
        def desc = DataProvider.DataDesc.builder()
                .separator(config.separator)
                .idIndex(config.idIndex)
                .labelIndex(config.labelIndex)
                .attributesAmount(config.attributesAmount)
                .colTypes(config.colTypes)
                .build()
        def labels = config.dataPath
                .collectMany { path ->
                    new NodeDataProvider(
                            path,
                            config.testDataPath,
                            desc,
                            true
                    ).training()
                }.collect { IdLabel.of(it.id, it.label) }

        and:
        def predictions = results.results
                .collect { IdLabel.of(it.id, it.value) }

        def metrics = new ClusteringMetrics(predictions, labels)
        def ari = metrics.adjustedRandIndex()
        ari >= 0
        ari <= 1

        and: "print results"
        println("ARI: $ari")

        and:
        def groups = results.results
                .collect { it.value }
                .unique()
        println("${groups.size()} groups: $groups")
    }

}
