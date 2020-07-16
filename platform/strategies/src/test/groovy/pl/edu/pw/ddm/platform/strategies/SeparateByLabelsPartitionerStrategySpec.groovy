package pl.edu.pw.ddm.platform.strategies

import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.strategy.TempFileCreator
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Path

class SeparateByLabelsPartitionerStrategySpec extends Specification {

    @Shared
    def tempFileCreator = new TempFileCreator()

    @Shared
    Path sourceFile

    def setupSpec() {
        sourceFile = tempFileCreator.create('setup.txt')
        sourceFile.append("1,A\n")
        sourceFile.append("2,A\n")
        sourceFile.append("3,A\n")
        sourceFile.append("4,B\n")
        sourceFile.append("5,B\n")
        sourceFile.append("6,B\n")
        sourceFile.append("7,C\n")
        sourceFile.append("8,C\n")
        sourceFile.append("9,C\n")
    }

    def cleanupSpec() {
        tempFileCreator.cleanup()
    }

    def "should prepare separate by labels data distribution"() {
        setup:
        def partitioner = new SeparateByLabelsPartitionerStrategy()
        def dataDesc = new PartitionerStrategy.DataDesc(
                numberOfSamples: 9,
                separator: ',',
                labelIndex: 1,
                filesLocations: [sourceFile.toString()]
        )
        def params = PartitionerStrategy.StrategyParameters.builder()
                .partitions(2)
                .customParams('A|B,C')
                .build()

        when:
        def partitions = partitioner.partition(dataDesc, params, tempFileCreator)

        then:
        partitions.size() == 2

        and:
        def lines0 = partitions[0].readLines()
        lines0.size() == 6
        lines0[0] == '1,A'
        lines0[1] == '2,A'
        lines0[2] == '3,A'
        lines0[3] == '4,B'
        lines0[4] == '5,B'
        lines0[5] == '6,B'

        def lines1 = partitions[1].readLines()
        lines1.size() == 3
        lines1[0] == '7,C'
        lines1[1] == '8,C'
        lines1[2] == '9,C'
    }

}
