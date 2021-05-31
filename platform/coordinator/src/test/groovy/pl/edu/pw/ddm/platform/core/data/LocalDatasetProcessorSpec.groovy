package pl.edu.pw.ddm.platform.core.data

import org.apache.commons.io.FileUtils
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

class LocalDatasetProcessorSpec extends Specification {

    @Shared
    def tempFileCreator = new TempFileCreator()

    def cleanupSpec() {
        tempFileCreator.cleanup()
    }

    def "should vectorize string attributes and add index to rows"() {
        given:
        def sourceDataFile = Paths.get(getClass().getResource('/some-various-2d-data.csv').path).toFile()
        def sourceData = FileUtils.readFileToByteArray(sourceDataFile)

        def path = tempFileCreator.create('tmpfiletest')
        Files.write(path, sourceData)

        def processor = new LocalDatasetProcessor(
                true,
                true,
                path,
                ',',
                null,
                0
        )

        when:
        processor.process()

        then:
        def lines1 = path.readLines()
        def lines2 = Paths.get(getClass().getResource('/some-various-2d-data_processed.csv').path).readLines()
        lines1 == lines2
    }

}
