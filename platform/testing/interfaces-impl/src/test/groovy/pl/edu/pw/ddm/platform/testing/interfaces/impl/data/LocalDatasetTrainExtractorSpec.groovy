package pl.edu.pw.ddm.platform.testing.interfaces.impl.data

import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.strategy.TempFileCreator
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Path

class LocalDatasetTrainExtractorSpec extends Specification {

    @Shared
    def tempFileCreator = new TempFileCreator()

    @Shared
    Path sourceFile

    def setupSpec() {
        sourceFile = tempFileCreator.create('setup.txt')
        100.times { sourceFile.append("$it\n") }
    }

    def cleanupSpec() {
        tempFileCreator.cleanup()
    }

    def "should divide data file on train and test 40/60 partitions"() {
        given:
        def extractor = new LocalDatasetTrainExtractor(sourceFile, 40, 18, tempFileCreator)

        when:
        extractor.extract()

        then:
        extractor.trainFile
        extractor.testFile
        def train = extractor.trainFile.readLines()
        def test = extractor.testFile.readLines()
        train.size() == 40
        test.size() == 60
    }

}
