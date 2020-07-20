package ddm.samples

import pl.edu.pw.ddm.platform.distfunc.EuclideanDistance
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.strategy.TempFileCreator
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Path

import static pl.edu.pw.ddm.platform.interfaces.data.DataProvider.AttributeType.NOMINAL
import static pl.edu.pw.ddm.platform.interfaces.data.DataProvider.AttributeType.NUMERIC

class DenseAndOutliersStrategySpec extends Specification {

    @Shared
    def tempFileCreator = new TempFileCreator()

    @Shared
    Path sourceFile

    def setupSpec() {
        sourceFile = tempFileCreator.create('setup.txt')
        sourceFile.append("1,0,A\n")
        sourceFile.append("2,1,A\n")
        sourceFile.append("3,5,A\n")
        sourceFile.append("4,2,B\n")
        sourceFile.append("5,2,B\n")
        sourceFile.append("6,8,B\n")
        sourceFile.append("7,7,C\n")
        sourceFile.append("8,5,C\n")
        sourceFile.append("9,17,C\n")
    }

    def cleanupSpec() {
        tempFileCreator.cleanup()
    }

    def "should prepare more dense data distribution"() {
        setup:
        def partitioner = new DenseAndOutliersStrategy()
        def dataDesc = new PartitionerStrategy.DataDesc(
                numberOfSamples: 9,
                separator: ',',
                idIndex: 0,
                labelIndex: 2,
                attributesAmount: 1,
                colTypes: [NUMERIC, NUMERIC, NOMINAL],
                filesLocations: [sourceFile.toString()]
        )
        def params = PartitionerStrategy.StrategyParameters.builder()
                .partitions(2)
                .customParams('0.5')
                .distanceFunction(new EuclideanDistance())
                .build()

        when:
        def partitions = partitioner.partition(dataDesc, params, tempFileCreator)

        then:
        partitions.size() == 2

        and:
        def lines0 = partitions[0].readLines()
        lines0.size() == 4
        lines0[0] == '2,1,A'
        lines0[1] == '4,2,B'
        lines0[2] == '5,2,B'
        lines0[3] == '7,7,C'


        def lines1 = partitions[1].readLines()
        lines1.size() == 5
        lines1[0] == '1,0,A'
        lines1[1] == '3,5,A'
        lines1[2] == '6,8,B'
        lines1[3] == '8,5,C'
        lines1[4] == '9,17,C'
    }

    def "should prepare more outliers data distribution"() {
        setup:
        def partitioner = new DenseAndOutliersStrategy()
        def dataDesc = new PartitionerStrategy.DataDesc(
                numberOfSamples: 9,
                separator: ',',
                idIndex: 0,
                labelIndex: 2,
                attributesAmount: 1,
                colTypes: [NUMERIC, NUMERIC, NOMINAL],
                filesLocations: [sourceFile.toString()]
        )
        def params = PartitionerStrategy.StrategyParameters.builder()
                .partitions(2)
                .customParams('0.9')
                .distanceFunction(new EuclideanDistance())
                .build()

        when:
        def partitions = partitioner.partition(dataDesc, params, tempFileCreator)

        then:
        partitions.size() == 2

        and:
        def lines0 = partitions[0].readLines()
        lines0.size() == 6
        lines0[0] == '2,1,A'
        lines0[1] == '1,0,A'
        lines0[2] == '4,2,B'
        lines0[3] == '5,2,B'
        lines0[4] == '7,7,C'
        lines0[5] == '8,5,C'


        def lines1 = partitions[1].readLines()
        lines1.size() == 3
        lines1[0] == '3,5,A'
        lines1[1] == '6,8,B'
        lines1[2] == '9,17,C'
    }

}
