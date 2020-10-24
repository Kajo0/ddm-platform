package pl.edu.pw.ddm.platform.strategies

import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.strategy.TempFileCreator
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Path

class UniformPartitionerStrategySpec extends Specification {

    @Shared
    def tempFileCreator = new TempFileCreator()

    @Shared
    Path sourceFile

    def setupSpec() {
        sourceFile = tempFileCreator.create('setup.txt')
        (1..9).each {
            sourceFile.append("$it\n")
        }
    }

    def cleanupSpec() {
        tempFileCreator.cleanup()
    }

    def "should prepare uniform data distribution"() {
        setup:
        def partitioner = new UniformPartitionerStrategy()
        def dataDesc = new PartitionerStrategy.DataDesc(
                numberOfSamples: 9,
                filesLocations: [sourceFile.toString()]
        )
        def params = PartitionerStrategy.StrategyParameters.builder()
                .partitions(3)
                .seed(10)
                .build()

        when:
        def partitions = partitioner.partition(dataDesc, params, tempFileCreator)

        then:
        partitions.size() == 3

        and:
        def lines0 = partitions[0].readLines()
        lines0.size() == 3
        lines0[0] == '5'
        lines0[1] == '7'
        lines0[2] == '9'

        def lines1 = partitions[1].readLines()
        lines1.size() == 3
        lines1[0] == '2'
        lines1[1] == '3'
        lines1[2] == '8'

        def lines2 = partitions[2].readLines()
        lines2.size() == 3
        lines2[0] == '1'
        lines2[1] == '4'
        lines2[2] == '6'
    }

}
