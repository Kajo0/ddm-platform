package pl.edu.pw.ddm.platform.algorithms.clustering.lct

import pl.edu.pw.ddm.platform.distfunc.EuclideanDistance
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider
import pl.edu.pw.ddm.platform.metrics.ClusteringMetrics
import pl.edu.pw.ddm.platform.metrics.dto.IdLabel
import pl.edu.pw.ddm.platform.testing.interfaces.impl.CentralRunner
import pl.edu.pw.ddm.platform.testing.interfaces.impl.ExecutionConfig
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.NodeDataProvider
import spock.lang.Specification

class LctTestingSpec extends Specification {

    def "should perform correct clustering of iris training data"() {
        given:
        def localProcessor = new LightweightLocalSiteProcessor()
        def globalProcessor = new LightweightAggregator()
        def miningMethod = new LightweightClusterer()

        and:
        def config = ExecutionConfig.builder()
                .localProcessor(localProcessor)
                .globalProcessor(globalProcessor)
                .miningMethod(miningMethod)
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
                        'groups'    : '3',
                        'iterations': '10',
                        'b'         : '2'
                ])
                .build()
        def cr = new CentralRunner(config)

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
        def localProcessor = new LightweightLocalSiteProcessor()
        def globalProcessor = new LightweightAggregator()
        def miningMethod = new LightweightClusterer()

        and:
        def config = ExecutionConfig.builder()
                .localProcessor(localProcessor)
                .globalProcessor(globalProcessor)
                .miningMethod(miningMethod)
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
                        'groups'    : '15',
                        'iterations': '10',
                        'b'         : '2'
                ])
                .build()
        def cr = new CentralRunner(config)

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
