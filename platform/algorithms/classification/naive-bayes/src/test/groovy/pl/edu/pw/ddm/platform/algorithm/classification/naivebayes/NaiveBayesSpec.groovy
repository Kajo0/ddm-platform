package pl.edu.pw.ddm.platform.algorithm.classification.naivebayes

import com.google.common.base.Stopwatch
import pl.edu.pw.ddm.platform.distfunc.EuclideanDistance
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy
import pl.edu.pw.ddm.platform.metrics.ClassificationMetrics
import pl.edu.pw.ddm.platform.metrics.dto.IdLabel
import pl.edu.pw.ddm.platform.strategies.UniformPartitionerStrategy
import pl.edu.pw.ddm.platform.testing.interfaces.impl.DdmExecutionConfig
import pl.edu.pw.ddm.platform.testing.interfaces.impl.DdmPipelineRunner
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.LocalDatasetTrainExtractor
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.NodeDataProvider
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.strategy.TempFileCreator
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

class NaiveBayesSpec extends Specification {

    @Shared
    def tempFileCreator = new TempFileCreator()

    def cleanupSpec() {
        tempFileCreator.cleanup()
    }

    def "should classify data"() {
        setup:
        def separator = '\t'
        def idIndex = 0
        def labelIndex = 3
        def attributesAmount = 2
        def trainPercent = 50

        def sourceFile = Paths.get(getClass().getResource('/small_2d.csv').path)

        def partitions = 4
        def seed = 123
        def execParams = [
                'seed': seed as String
        ]

        and:
        def timer = Stopwatch.createUnstarted()

        and:
        timer.start()
        println('START LocalDatasetTrainExtractor')
        def extractor = new LocalDatasetTrainExtractor(sourceFile, trainPercent, seed, tempFileCreator)
        extractor.extract()
        println("END LocalDatasetTrainExtractor ${timer.elapsed(TimeUnit.MILLISECONDS)}ms")
        timer.reset()

        and:
        def dataDesc = new PartitionerStrategy.DataDesc(
                idIndex: idIndex,
                labelIndex: labelIndex,
                separator: separator,
                attributesAmount: attributesAmount,
                filesLocations: [extractor.trainFile.toFile().path]
        )
        def params = PartitionerStrategy.StrategyParameters.builder()
                .partitions(partitions)
                .seed(seed)
                .build()
        def strategy = new UniformPartitionerStrategy()

        timer.start()
        println('START PartitionerStrategy')
        def dataPaths = strategy.partition(dataDesc, params, tempFileCreator).collect { it.toFile().path }
        println("END PartitionerStrategy ${timer.elapsed(TimeUnit.MILLISECONDS)}ms")
        timer.reset()

        and:
        def pipeline = new NaiveBayes()
        def miningMethod = new GlobalClassifier(null)

        and:
        def config = DdmExecutionConfig.builder()
                .algorithmConfig(pipeline)
                .miningMethod(miningMethod)
                .dataPath(dataPaths)
                .testDataPath(extractor.testFile.toFile().path)
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
