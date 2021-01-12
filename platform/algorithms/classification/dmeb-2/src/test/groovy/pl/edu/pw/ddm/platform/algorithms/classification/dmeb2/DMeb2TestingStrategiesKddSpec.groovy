package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2

import com.google.common.base.Stopwatch
import pl.edu.pw.ddm.platform.distfunc.EuclideanDistance
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy
import pl.edu.pw.ddm.platform.metrics.ClassificationMetrics
import pl.edu.pw.ddm.platform.metrics.dto.IdLabel
import pl.edu.pw.ddm.platform.strategies.UniformPartitionerStrategy
import pl.edu.pw.ddm.platform.strategies.mostof.MostOfOnePlusSomePartitionerStrategy
import pl.edu.pw.ddm.platform.testing.interfaces.impl.DdmExecutionConfig
import pl.edu.pw.ddm.platform.testing.interfaces.impl.DdmPipelineRunner
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.LocalDatasetProcessor
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.LocalDatasetTrainExtractor
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.NodeDataProvider
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.strategy.TempFileCreator
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

class DMeb2TestingStrategiesKddSpec extends Specification {

    @Shared
    def tempFileCreator = new TempFileCreator()

    def cleanupSpec() {
        tempFileCreator.cleanup()
    }

    def "should scatter data among files and perform correct classifications of kdd training data"() {
        setup:
        def separator = ','
        def idIndex = 0
        def labelIndex = 1
        def seed = 10
        def vectorize = false

        def sourceFile = Paths.get(getClass().getResource('/Postures_processed.csv').path)
//        def tmpFile = tempFileCreator.create('dummy')
//        tmpFile.write(new String(sourceFile.readBytes()))
//        sourceFile = tmpFile

        and:
        def timer = Stopwatch.createUnstarted()

        and:
//        timer.start()
//        println('START LocalDatasetProcessor')
//        def processor = new LocalDatasetProcessor(idIndex == null, vectorize, sourceFile, separator, idIndex, labelIndex)
//        processor.process()
//        println("END LocalDatasetProcessor ${timer.elapsed(TimeUnit.MILLISECONDS)}ms")
//        timer.reset()
//        def attributesAmount = processor.attributesAmount
        def attributesAmount = 37
        if (idIndex == null) {
            idIndex = 0
            ++labelIndex
        }

        and:
        timer.start()
        println('START LocalDatasetTrainExtractor')
        def extractor = new LocalDatasetTrainExtractor(sourceFile, 40, seed, tempFileCreator)
        extractor.extract()
        println("END LocalDatasetTrainExtractor ${timer.elapsed(TimeUnit.MILLISECONDS)}ms")
        timer.reset()

        and:
        def params = PartitionerStrategy.StrategyParameters.builder()
                .partitions(5)
                .customParams('fillEmptyButPercent=0.99;additionalClassesNumber=0;additionalClassesPercent=0;emptyWorkerFill=200')
                .seed(seed)
                .build()
        def dataDesc = new PartitionerStrategy.DataDesc(
                idIndex: idIndex,
                labelIndex: labelIndex,
                separator: separator,
                filesLocations: [extractor.trainFile.toFile().path]
        )
        def strategy = new MostOfOnePlusSomePartitionerStrategy()
        timer.start()
        println('START PartitionerStrategy')
        def dataPaths = strategy.partition(dataDesc, params, tempFileCreator).collect { it.toFile().path }
        println("END PartitionerStrategy ${timer.elapsed(TimeUnit.MILLISECONDS)}ms")
        timer.reset()

        and:
        def pipeline = new DMeb2()
        def miningMethod = new GlobalClassifier(null, null, null)

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
                .executionParams([
                        'seed'                                    : seed as String,
                        'meb_clusters'                            : '-1',
                        'kernel'                                  : 'linear',
                        'knn_k'                                   : '5',
                        'init_kmeans_method'                      : 'k-means++',
                        'use_local_classifier'                    : 'false',
                        'use_first_level_only'                    : 'false',
                        'global_normalization'                    : 'false',
                        'local_method_for_svs_clusters'           : 'close_to',
//                        'local_method_for_svs_clusters'           : 'leave_border',
//                        'local_method_for_svs_clusters'           : 'all_with',
//                        'local_method_for_svs_clusters'           : 'all_when_multi',
                        'close_to_percent'                        : '0.2',
                        'local_method_for_non_multiclass_clusters': 'metrics_collect',
//                        'local_method_for_non_multiclass_clusters': 'random',
//                        'local_method_for_non_multiclass_clusters': 'squash_to_centroid',
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
