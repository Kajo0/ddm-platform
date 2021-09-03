package pl.edu.pw.ddm.platform.algorithm.classification.naivebayes


import pl.edu.pw.ddm.platform.distfunc.EuclideanDistance
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy
import pl.edu.pw.ddm.platform.metrics.ClassificationMetrics
import pl.edu.pw.ddm.platform.metrics.dto.IdLabel
import pl.edu.pw.ddm.platform.strategies.UniformPartitionerStrategy
import pl.edu.pw.ddm.platform.testing.interfaces.impl.DdmExecutionConfig
import pl.edu.pw.ddm.platform.testing.interfaces.impl.DdmPipelineRunner
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.NodeDataProvider
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.strategy.TempFileCreator
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Paths

class NaiveBayesManualSpec extends Specification {

    @Shared
    def tempFileCreator = new TempFileCreator()

    def cleanupSpec() {
        tempFileCreator.cleanup()
    }

    @Unroll
    def "should classify data"() {
        setup:
        def separator = ','
        def idIndex = 0
        def labelIndex = 3
        def attributesAmount = 2

        def trainData = Paths.get(getClass().getResource('/manual_2d.csv').path)
        def testData = Paths.get(getClass().getResource('/manual_2d-test.csv').path)

        def partitions = 3
        def seed = 123
        def execParams = [
                'seed': seed as String
        ]

        and:
        def dataDesc = new PartitionerStrategy.DataDesc(
                idIndex: idIndex,
                labelIndex: labelIndex,
                separator: separator,
                attributesAmount: attributesAmount,
                filesLocations: [trainData.toFile().path]
        )
        def params = PartitionerStrategy.StrategyParameters.builder()
                .partitions(partitions)
                .seed(seed)
                .build()
        def strategy = new UniformPartitionerStrategy()
        def dataPaths = strategy.partition(dataDesc, params, tempFileCreator).collect { it.toFile().path }

        and:
        def pipeline = new NaiveBayes()
        def miningMethod = new GlobalClassifier(null)

        and:
        def config = DdmExecutionConfig.builder()
                .algorithmConfig(pipeline)
                .miningMethod(miningMethod)
                .dataPath(dataPaths)
                .testDataPath(testData.toFile().path)
                .separator(separator)
                .idIndex(idIndex)
                .labelIndex(labelIndex)
                .attributesAmount(attributesAmount)
                .colTypes(null) // null to use deduct
                .distanceFunction(new EuclideanDistance())
                .executionParams(execParams)
                .build()
        def cr = new DdmPipelineRunner(config)

        when:
        def results = cr.run()

        then:
        def model = (GModel) cr.masterNodeRunner.miningMethod.model
        model.labelColStats['-1'][0].mean == 1.8d
        model.labelColStats['-1'][0].variance == 0.6222222222222222d
        model.labelColStats['-1'][1].mean == 1.8d
        model.labelColStats['-1'][1].variance == 0.6222222222222222d
        model.labelColStats['-2'][0].mean == 104.0d
        model.labelColStats['-2'][0].variance == 17.714285714285708d
        model.labelColStats['-2'][1].mean == 102.0d
        model.labelColStats['-2'][1].variance == 0.5714285714285711d
        model.labelColStats['-3'][0].mean == 22.4d
        model.labelColStats['-3'][0].variance == 3.822222222222222d
        model.labelColStats['-3'][1].mean == 83.6d
        model.labelColStats['-3'][1].variance == 9.155555555555573d

        and:
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
        ).test()
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
