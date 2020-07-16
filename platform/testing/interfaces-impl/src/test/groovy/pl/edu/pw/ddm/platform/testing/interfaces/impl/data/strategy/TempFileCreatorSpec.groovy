package pl.edu.pw.ddm.platform.testing.interfaces.impl.data.strategy

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class TempFileCreatorSpec extends Specification {

    Path path

    def cleanup() {
        Files.deleteIfExists(path)
    }

    def "should create temporary file"() {
        given:
        def creator = new TempFileCreator()

        when:
        path = creator.create()

        then:
        Files.exists(path)
    }

    def "should create temporary file with suffix"() {
        given:
        def creator = new TempFileCreator()

        when:
        path = creator.create('suffix')

        then:
        Files.exists(path)

        and:
        path.fileName.toString().endsWith('suffix')
    }

}
