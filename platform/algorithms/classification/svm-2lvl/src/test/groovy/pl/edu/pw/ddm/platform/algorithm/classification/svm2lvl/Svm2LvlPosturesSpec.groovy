package pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl

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
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.LocalDatasetTrainExtractor
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.NodeDataProvider
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.strategy.TempFileCreator
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

class Svm2LvlPosturesSpec extends Specification {

    @Shared
    def tempFileCreator = new TempFileCreator()

    def cleanupSpec() {
        tempFileCreator.cleanup()
    }

    // ignore due to time consuming tests
    @Ignore
    @Unroll
    def "should #l_method #filtering_method - 1st=#first_lvl scatter data among files and perform correct classifications of training data"() {
        setup:
        def separator = ','
        def idIndex = 0
        def labelIndex = 1
        def attributesAmount = 37
        def trainPercent = 40

        def sourceFile = Paths.get(getClass().getResource('/Postures_processed.csv').path)

        def partitions = 8
        def strategies = [
                'uniform'              : '',
                'most-of-one-plus-some': 'fillEmptyButPercent=0.8;additionalClassesNumber=-2;additionalClassesPercent=0.05;emptyWorkerFill=1',
                'separated'            : 'emptyWorkerFill=1;fillEmptyButPercent=0.5;additionalClassesNumber=0;additionalClassesPercent=0',
                'most-plus-all'        : 'fillEmptyButPercent=0.6;additionalClassesNumber=-200;additionalClassesPercent=0.05;emptyWorkerFill=1',
                'all-but'              : 'fillEmptyButPercent=0.6;additionalClassesNumber=-3;additionalClassesPercent=0.05;emptyWorkerFill=1',
        ]
        def strategyAlias = 'separated'
        def seed = 123
        def execParams = [
                'seed'                       : seed as String,
                'debug'                      : 'false',
                'global_normalization'       : 'false',
                'use_local_classifier'       : 'false',
                'each_votes'                 : each_v,
                'use_first_global_level_only': first_lvl,
                'init_kmeans_method'         : 'k-means++',
                'kernel'                     : 'rbf',
                'groups'                     : groups,
                'knn_k'                      : knn_k,
                'local_filtering_method'     : filtering_method,
                'local_filtering_percent'    : r_local,
                'local_method_name'          : l_method,
                'random_percent'             : r_global,
                'global_expand_percent'      : '-0.1'
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
                filesLocations: [extractor.trainFile.toFile().path]
        )
        def params = PartitionerStrategy.StrategyParameters.builder()
                .partitions(partitions)
                .customParams(strategies[strategyAlias])
                .seed(seed)
                .build()
        def strategy = new UniformPartitionerStrategy()
        if (strategyAlias != 'uniform') {
            strategy = new MostOfOnePlusSomePartitionerStrategy()
        }

        timer.start()
        println('START PartitionerStrategy')
        def dataPaths = strategy.partition(dataDesc, params, tempFileCreator).collect { it.toFile().path }
        println("END PartitionerStrategy ${timer.elapsed(TimeUnit.MILLISECONDS)}ms")
        timer.reset()

        and:
        def pipeline = new Svm2Lvl()
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

        where:
        filtering_method | l_method              | r_local | r_global | first_lvl | each_v  | groups | knn_k
//        'none'                | ''
//        'random'              | ''
//        'class_random'        | ''
//        'partitioning_random' | ''
//        'none'                | 'just_random'
//        'none'                | 'centroids'
//        'none'                | 'centroids_per_class'
//        'none'                | 'convex_hull'
//        'none'                | 'only_with_svs'
        'none'           | 'just_random'         | '1.0'   | '0.1'    | 'false'   | 'false' | '-1'   | '-1'
        'none'           | 'centroids'           | '1.0'   | '1.0'    | 'false'   | 'false' | '-1'   | '-1'
        'none'           | 'centroids_per_class' | '1.0'   | '1.0'    | 'false'   | 'false' | '-1'   | '-1'
        'none'           | 'just_random'         | '1.0'   | '0.1'    | 'false'   | 'true'  | '-1'   | '-1'
        'none'           | 'centroids'           | '1.0'   | '1.0'    | 'false'   | 'true'  | '-1'   | '-1'
        'none'           | 'centroids_per_class' | '1.0'   | '1.0'    | 'false'   | 'true'  | '-1'   | '-1'
    }

}
