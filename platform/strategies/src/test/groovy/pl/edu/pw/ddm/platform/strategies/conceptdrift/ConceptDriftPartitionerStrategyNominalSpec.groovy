package pl.edu.pw.ddm.platform.strategies.conceptdrift

import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.strategy.TempFileCreator
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class ConceptDriftPartitionerStrategyNominalSpec extends Specification implements LocalChartDebugPrinter {

    @Shared
    def tempFileCreator = new TempFileCreator()

    @Shared
    Path sourceFile

    def setupSpec() {
        sourceFile = tempFileCreator.create('setup.txt')
        def i = 0

        sourceFile.append("${++i},A,Meadow,jug,hanger\n")
        sourceFile.append("${++i},A,trader,Woodpecker,hanger\n")
        sourceFile.append("${++i},A,Meadow,Woodpecker,róża\n")
        sourceFile.append("${++i},A,Meadow,key,ring!\n")
        sourceFile.append("${++i},A,trader,jug,ring!\n")
        sourceFile.append("${++i},A,trader,jug,ring!\n")
        sourceFile.append("${++i},A,krzak,jug,ring!\n")
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
                colTypes: ['nominal', 'nominal', 'nominal', 'nominal', 'nominal'],
                filesLocations: [sourceFile.toString()]
        )
        def params = PartitionerStrategy.StrategyParameters.builder()
                .partitions(4)
                .seed(10)
                .customParams('drifts=2;discreteRanges=-1;label=A')
                .build()

        when:
        def partitions = partitioner.partition(dataDesc, params, tempFileCreator)

        then:
        Files.readAllLines(partitions[0]).stream().filter { it.contains('A') }.count() == 3
        Files.readAllLines(partitions[1]).stream().filter { it.contains('A') }.count() == 4
        Files.readAllLines(partitions[2]).stream().filter { it.contains('A') }.count() == 0
        Files.readAllLines(partitions[3]).stream().filter { it.contains('A') }.count() == 0

        and:
        partitioner.bucketedIdsForTestChart.driftToPairs[0].collect { it.id }.containsAll(['1', '2', '3'])
        partitioner.bucketedIdsForTestChart.driftToPairs[1].collect { it.id }.containsAll(['4', '5', '6', '7'])
    }

}
