package pl.edu.pw.ddm.platform.strategies.conceptdrift

import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.strategy.TempFileCreator
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ConceptDriftPartitionerStrategyIrisSpec extends Specification implements LocalChartDebugPrinter {

    @Shared
    def tempFileCreator = new TempFileCreator()

    @Shared
    Path sourceFile

    def setupSpec() {
        sourceFile = tempFileCreator.create('setup.txt')
        def i = 0

        def sourceDataFile = Paths.get(getClass().getResource('/iris.data').path).toFile()
        sourceDataFile.readLines()
                .each { sourceFile.append("${++i},$it\n") }
    }

    def cleanupSpec() {
        tempFileCreator.cleanup()
    }

    def "should prepare concept drift scattering"() {
        setup:
        def partitioner = new ConceptDriftPartitionerStrategy()
        def dataDesc = new PartitionerStrategy.DataDesc(
                numberOfSamples: Files.lines(tempFileCreator.files.first()).count(),
                separator: ',',
                idIndex: 0,
                labelIndex: 5,
                attributesAmount: 4,
                colTypes: ['numeric', 'numeric', 'numeric', 'numeric', 'numeric', 'nominal'],
                filesLocations: [sourceFile.toString()]
        )
        def params = PartitionerStrategy.StrategyParameters.builder()
                .partitions(4)
                .seed(10)
                .customParams('drifts=2;discreteRanges=10;label=Iris-virginica')
//                .customParams('drifts=3;discreteRanges=10;label=Iris-versicolor')
//                .customParams('drifts=2;discreteRanges=10;label=Iris-setosa')
                .build()

        when:
        def partitions = partitioner.partition(dataDesc, params, tempFileCreator)

        then:
        Files.readAllLines(partitions[0]).stream().filter { it.contains('Iris-virginica') }.count() == 27
        Files.readAllLines(partitions[1]).stream().filter { it.contains('Iris-virginica') }.count() == 23
        Files.readAllLines(partitions[2]).stream().filter { it.contains('Iris-virginica') }.count() == 0
        Files.readAllLines(partitions[3]).stream().filter { it.contains('Iris-virginica') }.count() == 0

        and:
        printXYChart(partitioner.bucketedIdsForTestChart.driftToPairs, 0, 1)
        printXYChart(partitioner.bucketedIdsForTestChart.driftToPairs, 1, 2)
        printXYChart(partitioner.bucketedIdsForTestChart.driftToPairs, 0, 2)
        printXYChart(partitioner.bucketedIdsForTestChart.driftToPairs, 0, 3)
        printXYChart(partitioner.bucketedIdsForTestChart.driftToPairs, 1, 3)
        printXYChart(partitioner.bucketedIdsForTestChart.driftToPairs, 2, 3)
        printChartEvery(partitioner.bucketedIdsForTestChart.driftToPairs, dataDesc.attributesAmount)
    }

}
