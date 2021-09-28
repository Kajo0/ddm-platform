package pl.edu.pw.ddm.platform.strategies.conceptdrift

import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.strategy.TempFileCreator
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class ConceptDriftPartitionerStrategySpec extends Specification implements LocalChartDebugPrinter {

    @Shared
    def tempFileCreator = new TempFileCreator()

    @Shared
    Path sourceFile

    def setupSpec() {
        sourceFile = tempFileCreator.create('setup.txt')
        def i = 0

        sourceFile.append("${++i},A,1,2,3\n")
        sourceFile.append("${++i},A,2,3,4\n")
        sourceFile.append("${++i},A,3,4,5\n")
        sourceFile.append("${++i},A,3,2,1\n")
        sourceFile.append("${++i},A,4,3,2\n")
        sourceFile.append("${++i},A,5,4,3\n")
        sourceFile.append("${++i},A,11,12,13\n")
        sourceFile.append("${++i},A,13,14,15\n")
        sourceFile.append("${++i},A,13,12,11\n")
        sourceFile.append("${++i},A,15,14,13\n")
        sourceFile.append("${++i},B,11,12,13\n") //11
        sourceFile.append("${++i},B,12,13,14\n") //12
        sourceFile.append("${++i},B,13,14,15\n") //13
        sourceFile.append("${++i},B,13,12,11\n") //14
        sourceFile.append("${++i},B,14,13,12\n") //15
        sourceFile.append("${++i},B,15,14,13\n") //16
        sourceFile.append("${++i},B,1,2,3\n")    //17
        sourceFile.append("${++i},B,3,4,5\n")    //18
        sourceFile.append("${++i},B,3,2,1\n")    //19
        sourceFile.append("${++i},B,5,4,3\n")    //20
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
                labelIndex: 1,
                attributesAmount: 3,
                colTypes: ['nominal', 'nominal', 'numeric', 'numeric', 'numeric'],
                filesLocations: [sourceFile.toString()]
        )
        def params = PartitionerStrategy.StrategyParameters.builder()
                .partitions(4)
                .seed(10)
                .customParams('drifts=3;discreteRanges=5;label=B')
                .build()

        when:
        def partitions = partitioner.partition(dataDesc, params, tempFileCreator)

        then:
        Files.readAllLines(partitions[0]).stream().filter { it.contains('B') }.count() == 3
        Files.readAllLines(partitions[1]).stream().filter { it.contains('B') }.count() == 3
        Files.readAllLines(partitions[2]).stream().filter { it.contains('B') }.count() == 4
        Files.readAllLines(partitions[3]).stream().filter { it.contains('B') }.count() == 0

        Files.readAllLines(partitions[0]).stream().filter { it.contains('A') }.count() == 2
        Files.readAllLines(partitions[1]).stream().filter { it.contains('A') }.count() == 5
        Files.readAllLines(partitions[2]).stream().filter { it.contains('A') }.count() == 2
        Files.readAllLines(partitions[3]).stream().filter { it.contains('A') }.count() == 1

        and:
        partitioner.bucketedIdsForTestChart.driftToPairs[0].collect { it.id }.containsAll(['17', '19', '20'])
        partitioner.bucketedIdsForTestChart.driftToPairs[1].collect { it.id }.containsAll(['18', '14', '15'])
        partitioner.bucketedIdsForTestChart.driftToPairs[2].collect { it.id }.containsAll(['11', '12', '13', '16'])

        and:
        printXYChart(partitioner.bucketedIdsForTestChart.driftToPairs, 0, 1)
        printXYChart(partitioner.bucketedIdsForTestChart.driftToPairs, 0, 2)
        printXYChart(partitioner.bucketedIdsForTestChart.driftToPairs, 1, 2)
        printChartEvery(partitioner.bucketedIdsForTestChart.driftToPairs, dataDesc.attributesAmount)
    }

}
