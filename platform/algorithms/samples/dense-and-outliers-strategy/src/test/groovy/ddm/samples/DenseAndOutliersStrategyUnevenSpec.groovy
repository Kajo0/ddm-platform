package ddm.samples

import pl.edu.pw.ddm.platform.distfunc.EuclideanDistance
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.strategy.TempFileCreator
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Path

import static pl.edu.pw.ddm.platform.interfaces.data.DataProvider.AttributeType.NOMINAL
import static pl.edu.pw.ddm.platform.interfaces.data.DataProvider.AttributeType.NUMERIC

class DenseAndOutliersStrategyUnevenSpec extends Specification {

    @Shared
    def tempFileCreator = new TempFileCreator()

    @Shared
    Path sourceFile

    def setupSpec() {
        sourceFile = tempFileCreator.create('setup.txt')
        def i = 0

        sourceFile.append("${i++},0,-10,A\n")
        sourceFile.append("${i++},2,-10,A\n")
        sourceFile.append("${i++},4,-10,A\n")
        sourceFile.append("${i++},6,-10,A\n")
        sourceFile.append("${i++},8,-10,A\n")
        sourceFile.append("${i++},10,-10,A\n")
        sourceFile.append("${i++},-10,0,A\n")
        sourceFile.append("${i++},-10,2,A\n")
        sourceFile.append("${i++},-10,4,A\n")
        sourceFile.append("${i++},-10,6,A\n")
        sourceFile.append("${i++},-10,8,A\n")
        sourceFile.append("${i++},-10,10,A\n")
        sourceFile.append("${i++},0,20,A\n")
        sourceFile.append("${i++},2,20,A\n")
        sourceFile.append("${i++},4,20,A\n")
        sourceFile.append("${i++},6,20,A\n")
        sourceFile.append("${i++},8,20,A\n")
        sourceFile.append("${i++},10,20,A\n")
        sourceFile.append("${i++},20,0,A\n")
        sourceFile.append("${i++},20,2,A\n")
        sourceFile.append("${i++},20,4,A\n")
        sourceFile.append("${i++},20,6,A\n")
        sourceFile.append("${i++},20,8,A\n")
        sourceFile.append("${i++},20,10,A\n")

        sourceFile.append("${i++},3,3,A\n")
        sourceFile.append("${i++},3,4,A\n")
        sourceFile.append("${i++},3,5,A\n")
        sourceFile.append("${i++},3,6,A\n")
        sourceFile.append("${i++},3,7,A\n")
        sourceFile.append("${i++},3,3,A\n")
        sourceFile.append("${i++},4,3,A\n")
        sourceFile.append("${i++},5,3,A\n")
        sourceFile.append("${i++},6,3,A\n")
        sourceFile.append("${i++},7,3,A\n")
        sourceFile.append("${i++},7,3,A\n")
        sourceFile.append("${i++},7,4,A\n")
        sourceFile.append("${i++},7,5,A\n")
        sourceFile.append("${i++},7,6,A\n")
        sourceFile.append("${i++},7,7,A\n")
        sourceFile.append("${i++},3,7,A\n")
        sourceFile.append("${i++},4,7,A\n")
        sourceFile.append("${i++},5,7,A\n")
        sourceFile.append("${i++},6,7,A\n")
        sourceFile.append("${i++},7,7,A\n")

        sourceFile.append("${i++},4,4,A\n")
        sourceFile.append("${i++},4,5,A\n")
        sourceFile.append("${i++},4,6,A\n")
        sourceFile.append("${i++},4,4,A\n")
        sourceFile.append("${i++},5,4,A\n")
        sourceFile.append("${i++},6,4,A\n")
        sourceFile.append("${i++},6,4,A\n")
        sourceFile.append("${i++},6,5,A\n")
        sourceFile.append("${i++},6,6,A\n")
        sourceFile.append("${i++},4,6,A\n")
        sourceFile.append("${i++},5,6,A\n")
        sourceFile.append("${i++},6,6,A\n")
        sourceFile.append("${i++},5,5,A\n")
    }

    def cleanupSpec() {
        tempFileCreator.cleanup()
    }

    def "should prepare split of dense data distribution on uneven number of nodes by diving dense part on more nodes"() {
        setup:
        def partitioner = new DenseAndOutliersStrategy()
        def dataDesc = new PartitionerStrategy.DataDesc(
                numberOfSamples: 57,
                separator: ',',
                idIndex: 0,
                labelIndex: 3,
                attributesAmount: 2,
                colTypes: [NUMERIC, NUMERIC, NUMERIC, NOMINAL],
                filesLocations: [sourceFile.toString()]
        )
        def params = PartitionerStrategy.StrategyParameters.builder()
                .partitions(3)
                .customParams('0.5')
                .seed(123)
                .distanceFunction(new EuclideanDistance())
                .build()

        when:
        def partitions = partitioner.partition(dataDesc, params, tempFileCreator)

        then:
        partitions.size() == 3

        and:
        def lines0 = partitions[0].readLines()
        lines0.size() == 21
        lines0[0] == '45,4,5,A'
        lines0[1] == '48,5,4,A'
        lines0[2] == '51,6,5,A'
        lines0[3] == '49,6,4,A'
        lines0[4] == '50,6,4,A'
        lines0[5] == '52,6,6,A'
        lines0[6] == '53,4,6,A'
        lines0[7] == '31,5,3,A'
        lines0[8] == '36,7,5,A'
        lines0[9] == '27,3,6,A'
        lines0[10] == '32,6,3,A'
        lines0[11] == '35,7,4,A'
        lines0[12] == '37,7,6,A'
        lines0[13] == '42,6,7,A'
        lines0[14] == '24,3,3,A'
        lines0[15] == '28,3,7,A'
        lines0[16] == '29,3,3,A'
        lines0[17] == '33,7,3,A'
        lines0[18] == '38,7,7,A'
        lines0[19] == '39,3,7,A'
        lines0[20] == '43,7,7,A'

        def lines1 = partitions[1].readLines()
        lines1.size() == 12
        lines1[0] == '56,5,5,A'
        lines1[1] == '54,5,6,A'
        lines1[2] == '44,4,4,A'
        lines1[3] == '46,4,6,A'
        lines1[4] == '47,4,4,A'
        lines1[5] == '55,6,6,A'
        lines1[6] == '26,3,5,A'
        lines1[7] == '41,5,7,A'
        lines1[8] == '25,3,4,A'
        lines1[9] == '30,4,3,A'
        lines1[10] == '40,4,7,A'
        lines1[11] == '34,7,3,A'

        def lines2 = partitions[2].readLines()
        lines2.size() == 24
        lines2[0] == '2,4,-10,A'
        lines2[1] == '3,6,-10,A'
        lines2[2] == '8,-10,4,A'
        lines2[3] == '9,-10,6,A'
        lines2[4] == '14,4,20,A'
        lines2[5] == '15,6,20,A'
        lines2[6] == '20,20,4,A'
        lines2[7] == '21,20,6,A'
        lines2[8] == '1,2,-10,A'
        lines2[9] == '4,8,-10,A'
        lines2[10] == '7,-10,2,A'
        lines2[11] == '10,-10,8,A'
        lines2[12] == '13,2,20,A'
        lines2[13] == '16,8,20,A'
        lines2[14] == '19,20,2,A'
        lines2[15] == '22,20,8,A'
        lines2[16] == '0,0,-10,A'
        lines2[17] == '5,10,-10,A'
        lines2[18] == '6,-10,0,A'
        lines2[19] == '11,-10,10,A'
        lines2[20] == '12,0,20,A'
        lines2[21] == '17,10,20,A'
        lines2[22] == '18,20,0,A'
        lines2[23] == '23,20,10,A'
    }

}
