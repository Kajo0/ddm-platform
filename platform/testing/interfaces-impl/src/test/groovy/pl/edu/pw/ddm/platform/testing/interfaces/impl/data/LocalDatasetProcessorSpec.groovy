package pl.edu.pw.ddm.platform.testing.interfaces.impl.data

import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.strategy.TempFileCreator
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Path

class LocalDatasetProcessorSpec extends Specification {

    @Shared
    def tempFileCreator = new TempFileCreator()

    @Shared
    Path sourceFile

    def setupSpec() {
        sourceFile = tempFileCreator.create('setup.txt')
        sourceFile.append("8,A,88,ala\n")
        sourceFile.append("7,A,88,ma\n")
        sourceFile.append("6,A,88,kota\n")
        sourceFile.append("5,B,88,ala\n")
        sourceFile.append("4,B,88,ma\n")
        sourceFile.append("3,B,88,kota\n")
        sourceFile.append("2,C,88,ala\n")
        sourceFile.append("1,C,88,ma\n")
        sourceFile.append("0,C,88,kota\n")
    }

    def cleanupSpec() {
        tempFileCreator.cleanup()
    }

    def "should add index and vectorize data"() {
        given:
        def processor = new LocalDatasetProcessor(true, true, sourceFile, ',', null, 1)

        when:
        processor.process()

        then:
        def lines = sourceFile.readLines()
        lines.size() == 9
        lines[0] == '0,8,0,88,0'
        lines[1] == '1,7,0,88,1'
        lines[2] == '2,6,0,88,2'
        lines[3] == '3,5,1,88,0'
        lines[4] == '4,4,1,88,1'
        lines[5] == '5,3,1,88,2'
        lines[6] == '6,2,2,88,0'
        lines[7] == '7,1,2,88,1'
        lines[8] == '8,0,2,88,2'

        and:
        processor.attributesAmount == 3
    }

}
