package pl.edu.pw.ddm.platform.strategies.conceptdrift

import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.strategy.TempFileCreator
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ConceptDriftPartitionerStrategy2dSpec extends Specification implements LocalChartDebugPrinter {

    @Shared
    def tempFileCreator = new TempFileCreator()

    @Shared
    Path sourceFile

    def setupSpec() {
        sourceFile = tempFileCreator.create('setup.txt')
        def i = 0

        def sourceDataFile = Paths.get(getClass().getResource('/2d-numerical.csv').path).toFile()
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
                labelIndex: 3,
                attributesAmount: 2,
                colTypes: ['numeric', 'numeric', 'numeric', 'nominal'],
                filesLocations: [sourceFile.toString()]
        )
        def params = PartitionerStrategy.StrategyParameters.builder()
                .partitions(4)
                .seed(10)
                .customParams('drifts=3;discreteRanges=40;label=1')
                .build()

        when:
        def partitions = partitioner.partition(dataDesc, params, tempFileCreator)

        then:
        Files.readAllLines(partitions[0]).stream().filter { it.endsWith('1') }.count() == 164
        Files.readAllLines(partitions[1]).stream().filter { it.endsWith('1') }.count() == 166
        Files.readAllLines(partitions[2]).stream().filter { it.endsWith('1') }.count() == 163
        Files.readAllLines(partitions[3]).stream().filter { it.endsWith('1') }.count() == 0

        and:
        printXYChart(partitioner.bucketedIdsForTestChart.driftToPairs, 0, 1)
        printChartEvery(partitioner.bucketedIdsForTestChart.driftToPairs, dataDesc.attributesAmount)
    }

}
