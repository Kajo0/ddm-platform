package pl.edu.pw.ddm.platform.strategies.mostof

import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy
import pl.edu.pw.ddm.platform.strategies.mostof.MostOfOnePlusSomePartitionerStrategy
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.strategy.TempFileCreator
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class MostOfOnePlusSomePartitionerStrategySpec extends Specification {

    @Shared
    def tempFileCreator = new TempFileCreator()

    @Shared
    Path sourceFile

    def setupSpec() {
        sourceFile = tempFileCreator.create('setup.txt')
        (1..10).each { sourceFile.append("$it,A\n") }
        (11..20).each { sourceFile.append("$it,B\n") }
        (21..30).each { sourceFile.append("$it,C\n") }
        (31..40).each { sourceFile.append("$it,D\n") }
    }


    def cleanupSpec() {
        tempFileCreator.cleanup()
    }

    def "should prepare most of partitions when labels < workers"() {
        setup:
        def partitioner = new MostOfOnePlusSomePartitionerStrategy()
        def dataDesc = new PartitionerStrategy.DataDesc(
                numberOfSamples: Files.lines(tempFileCreator.files.first()).count(),
                separator: ',',
                labelIndex: 1,
                filesLocations: [sourceFile.toString()]
        )
        def params = PartitionerStrategy.StrategyParameters.builder()
                .partitions(4)
                .seed(10)
                .customParams('fillEmptyButPercent=0.8;additionalClassesNumber=1;additionalClassesPercent=0.1')
                .build()

        when:
        def partitions = partitioner.partition(dataDesc, params, tempFileCreator)

        then:
        Files.readAllLines(partitions[0]).stream().count() == 10
        Files.readAllLines(partitions[1]).stream().count() == 10
        Files.readAllLines(partitions[2]).stream().count() == 10
        Files.readAllLines(partitions[3]).stream().count() == 10
    }

    def "should prepare separate label for every partitions when labels == workers"() {
        setup:
        def partitioner = new MostOfOnePlusSomePartitionerStrategy()
        def dataDesc = new PartitionerStrategy.DataDesc(
                numberOfSamples: Files.lines(tempFileCreator.files.first()).count(),
                separator: ',',
                labelIndex: 1,
                filesLocations: [sourceFile.toString()]
        )
        def params = PartitionerStrategy.StrategyParameters.builder()
                .partitions(4)
                .seed(10)
                .customParams('fillEmptyButPercent=0.99;additionalClassesNumber=0;additionalClassesPercent=0')
                .build()

        when:
        def partitions = partitioner.partition(dataDesc, params, tempFileCreator)

        then:
        Files.readAllLines(partitions[0]).stream().count() == 10
        Files.readAllLines(partitions[0]).stream().filter { it.endsWith('A') }.count() == 10
        Files.readAllLines(partitions[1]).stream().count() == 10
        Files.readAllLines(partitions[1]).stream().filter { it.endsWith('B') }.count() == 10
        Files.readAllLines(partitions[2]).stream().count() == 10
        Files.readAllLines(partitions[2]).stream().filter { it.endsWith('C') }.count() == 10
        Files.readAllLines(partitions[3]).stream().count() == 10
        Files.readAllLines(partitions[3]).stream().filter { it.endsWith('D') }.count() == 10
    }

}
