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
        def sourceDataFile = Paths.get(getClass().getResource('/some-various-data.csv').path).toFile()
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
        def lines2 = Paths.get(getClass().getResource('/some-various-data_processed.csv').path).readLines()
        lines1 == lines2
    }

    def "should expand data"() {
        given:
        def sourceDataFile = Paths.get(getClass().getResource('/some-2d-data.csv').path).toFile()
        def sourceData = FileUtils.readFileToByteArray(sourceDataFile)

        def path = tempFileCreator.create('tmpfiletest')
        Files.write(path, sourceData)

        def processor = new LocalDatasetProcessor(
                false,
                false,
                path,
                '\t',
                0,
                3
        )

        when:
        processor.process()
        processor.expand(10, null)

        then:
        processor.statistics
        with(processor.statistics) {
            it.classMeanMap == ['LABEL1': [97.4, 104.6] as double[],
                                'LABEL2': [6.2, 14.6] as double[]]
            it.classStdDevMap == ['LABEL1': [4.2708313008125245, 5.642694391866353] as double[],
                                  'LABEL2': [4.664761515876241, 5.642694391866354] as double[]]
        }
    }

}
