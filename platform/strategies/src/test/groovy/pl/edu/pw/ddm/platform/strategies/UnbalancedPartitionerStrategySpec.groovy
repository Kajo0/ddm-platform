package pl.edu.pw.ddm.platform.strategies

import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.strategy.TempFileCreator
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class UnbalancedPartitionerStrategySpec extends Specification {

    @Shared
    def tempFileCreator = new TempFileCreator()

    @Shared
    Path sourceFile

    def setupSpec() {
        sourceFile = tempFileCreator.create('setup.txt')
        (1..50).each { sourceFile.append("$it,A\n") }
        (51..100).each { sourceFile.append("$it,B\n") }
        (101..150).each { sourceFile.append("$it,C\n") }
        (151..200).each { sourceFile.append("$it,D\n") }
    }


    def cleanupSpec() {
        tempFileCreator.cleanup()
    }

    def "should count correct amounts"() {
        setup:
        def partitioner = new UnbalancedPartitionerStrategy()

        and:
        partitioner.nodeThreshold = nodeThreshold
        partitioner.unbalancedness = unbalancedness
        partitioner.workers = workers

        expect:
        partitioner.countLargeAmount(sampleCount) == large
        partitioner.countSmallAmount(sampleCount, large) == small
        partitioner.countRestAmount(sampleCount, large, small) == rest

        where:
        nodeThreshold | unbalancedness | workers | sampleCount || large | small | rest
        1             | 0.5            | 2       | 100         || 66    | 34    | 0
        1             | 0.33           | 2       | 100         || 75    | 25    | 0
        1             | 0.5            | 3       | 100         || 50    | 25    | 0
        2             | 0.5            | 3       | 100         || 40    | 20    | 0
        1             | 0.5            | 10      | 100         || 18    | 9     | 1
        2             | 0.1            | 5       | 100         || 43    | 4     | 2
        2             | 0.9            | 5       | 100         || 21    | 19    | 1
        1             | 0.01           | 100     | 50          || 25    | 0     | 25
    }

    def "should count correct amounts with sum validation"() {
        setup:
        def partitioner = new UnbalancedPartitionerStrategy()

        and:
        partitioner.nodeThreshold = nodeThreshold
        partitioner.unbalancedness = unbalancedness
        partitioner.workers = workers

        expect:
        partitioner.calculateRatioAmounts(sampleCount)
        partitioner.largeAmount == ['': large]
        partitioner.smallAmount == ['': small]
        partitioner.restAmount == ['': rest]

        where:
        nodeThreshold | unbalancedness | workers | sampleCount || large | small | rest
        1             | 0.5            | 2       | 100         || 66    | 34    | 0
        1             | 0.33           | 2       | 100         || 75    | 25    | 0
        1             | 0.5            | 3       | 100         || 50    | 25    | 0
        2             | 0.5            | 3       | 100         || 40    | 20    | 0
        1             | 0.5            | 10      | 100         || 18    | 9     | 1
        2             | 0.1            | 5       | 100         || 43    | 4     | 2
        2             | 0.9            | 5       | 100         || 21    | 19    | 1
        2             | 0.1            | 4       | 200         || 90    | 10    | 0
        1             | 0.1            | 4       | 200         || 153   | 15    | 2
    }

    def "should throw exception when unable to calculate ratio"() {
        setup:
        def partitioner = new UnbalancedPartitionerStrategy()

        and:
        partitioner.nodeThreshold = nodeThreshold
        partitioner.unbalancedness = unbalancedness
        partitioner.workers = workers

        when:
        partitioner.calculateRatioAmounts(sampleCount)

        then:
        thrown(IllegalStateException)

        and:
        partitioner.largeAmount == ['': large]
        partitioner.smallAmount == ['': small]
        partitioner.restAmount == ['': rest]

        where:
        nodeThreshold | unbalancedness | workers | sampleCount || large | small | rest
        1             | 0.01           | 100     | 50          || 25    | 0     | 25
    }

    def "should count correct proportional amounts with sum validation"() {
        setup:
        def partitioner = new UnbalancedPartitionerStrategy()

        and:
        partitioner.nodeThreshold = nodeThreshold
        partitioner.unbalancedness = unbalancedness
        partitioner.proportional = true
        partitioner.workers = workers
        partitioner.labels = labels

        and:
        def sampleCount = labels.values().sum()

        expect:
        partitioner.calculateProportionalRatioAmounts(sampleCount)
        partitioner.largeAmount == large
        partitioner.smallAmount == small
        partitioner.restAmount == rest

        where:
        nodeThreshold | unbalancedness | workers | labels                 || large               | small              | rest
        1             | 0.33           | 2       | ['A': 100L, 'B': 100L] || ['A': 75, 'B': 75]  | ['A': 25, 'B': 25] | ['A': 0, 'B': 0]
        2             | 0.4            | 3       | ['A': 100L, 'B': 100L] || ['A': 41, 'B': 41]  | ['A': 18, 'B': 18] | ['A': 0, 'B': 0]
        1             | 0.5            | 10      | ['A': 100L, 'B': 100L] || ['A': 18, 'B': 18]  | ['A': 9, 'B': 9]   | ['A': 1, 'B': 1]
        1             | 0.33           | 2       | ['A': 150L, 'B': 100L] || ['A': 112, 'B': 75] | ['A': 38, 'B': 25] | ['A': 0, 'B': 0]
    }

    def "should prepare proportional label scattering"() {
        setup:
        def partitioner = new UnbalancedPartitionerStrategy()
        def dataDesc = new PartitionerStrategy.DataDesc(
                numberOfSamples: Files.lines(tempFileCreator.files.first()).count(),
                separator: ',',
                idIndex: 0,
                labelIndex: 1,
                filesLocations: [sourceFile.toString()]
        )
        def params = PartitionerStrategy.StrategyParameters.builder()
                .partitions(4)
                .seed(10)
                .customParams('proportional=1;nodeThreshold=1;unbalancedness=0.1')
                .build()

        when:
        def partitions = partitioner.partition(dataDesc, params, tempFileCreator)

        then:
        Files.readAllLines(partitions[0]).stream().count() == 152
        Files.readAllLines(partitions[0]).stream().filter { it.endsWith('A') }.count() == 38
        Files.readAllLines(partitions[0]).stream().filter { it.endsWith('B') }.count() == 38
        Files.readAllLines(partitions[0]).stream().filter { it.endsWith('C') }.count() == 38
        Files.readAllLines(partitions[0]).stream().filter { it.endsWith('D') }.count() == 38
        Files.readAllLines(partitions[1]).stream().count() == 16
        Files.readAllLines(partitions[1]).stream().filter { it.endsWith('A') }.count() == 4
        Files.readAllLines(partitions[1]).stream().filter { it.endsWith('B') }.count() == 4
        Files.readAllLines(partitions[1]).stream().filter { it.endsWith('C') }.count() == 4
        Files.readAllLines(partitions[1]).stream().filter { it.endsWith('D') }.count() == 4
        Files.readAllLines(partitions[2]).stream().count() == 16
        Files.readAllLines(partitions[2]).stream().filter { it.endsWith('A') }.count() == 4
        Files.readAllLines(partitions[2]).stream().filter { it.endsWith('B') }.count() == 4
        Files.readAllLines(partitions[2]).stream().filter { it.endsWith('C') }.count() == 4
        Files.readAllLines(partitions[2]).stream().filter { it.endsWith('D') }.count() == 4
        Files.readAllLines(partitions[3]).stream().count() == 16
        Files.readAllLines(partitions[3]).stream().filter { it.endsWith('A') }.count() == 4
        Files.readAllLines(partitions[3]).stream().filter { it.endsWith('B') }.count() == 4
        Files.readAllLines(partitions[3]).stream().filter { it.endsWith('C') }.count() == 4
        Files.readAllLines(partitions[3]).stream().filter { it.endsWith('D') }.count() == 4
    }

    def "should prepare non proportional label scattering"() {
        setup:
        def partitioner = new UnbalancedPartitionerStrategy()
        def dataDesc = new PartitionerStrategy.DataDesc(
                numberOfSamples: Files.lines(tempFileCreator.files.first()).count(),
                separator: ',',
                labelIndex: 1,
                filesLocations: [sourceFile.toString()]
        )
        def params = PartitionerStrategy.StrategyParameters.builder()
                .partitions(4)
                .seed(10)
                .customParams('proportional=0;nodeThreshold=2;unbalancedness=0.1')
                .build()

        when:
        def partitions = partitioner.partition(dataDesc, params, tempFileCreator)

        then:
        Files.readAllLines(partitions[0]).stream().count() == 90
        Files.readAllLines(partitions[0]).stream().filter { it.endsWith('A') }.count() == 23
        Files.readAllLines(partitions[0]).stream().filter { it.endsWith('B') }.count() == 21
        Files.readAllLines(partitions[0]).stream().filter { it.endsWith('C') }.count() == 18
        Files.readAllLines(partitions[0]).stream().filter { it.endsWith('D') }.count() == 28
        Files.readAllLines(partitions[1]).stream().count() == 90
        Files.readAllLines(partitions[1]).stream().filter { it.endsWith('A') }.count() == 23
        Files.readAllLines(partitions[1]).stream().filter { it.endsWith('B') }.count() == 23
        Files.readAllLines(partitions[1]).stream().filter { it.endsWith('C') }.count() == 23
        Files.readAllLines(partitions[1]).stream().filter { it.endsWith('D') }.count() == 21
        Files.readAllLines(partitions[2]).stream().count() == 10
        Files.readAllLines(partitions[2]).stream().filter { it.endsWith('A') }.count() == 3
        Files.readAllLines(partitions[2]).stream().filter { it.endsWith('B') }.count() == 2
        Files.readAllLines(partitions[2]).stream().filter { it.endsWith('C') }.count() == 4
        Files.readAllLines(partitions[2]).stream().filter { it.endsWith('D') }.count() == 1
        Files.readAllLines(partitions[3]).stream().count() == 10
        Files.readAllLines(partitions[3]).stream().filter { it.endsWith('A') }.count() == 1
        Files.readAllLines(partitions[3]).stream().filter { it.endsWith('B') }.count() == 4
        Files.readAllLines(partitions[3]).stream().filter { it.endsWith('C') }.count() == 5
        Files.readAllLines(partitions[3]).stream().filter { it.endsWith('D') }.count() == 0
    }

}
