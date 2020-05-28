package pl.edu.pw.ddm.platform.testing.interfaces.impl.data

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class NodeResultCollectorSpec extends Specification {

    Path resultsFile

    def setup() {
        resultsFile = Files.createTempFile('testResults', '.txt')
    }

    def cleanup() {
        Files.delete(resultsFile)
    }

    def "should collect results"() {
        given:
        def collector = new NodeResultCollector()

        when:
        collector.collect('1', 1)
        collector.collect('2', 2.1)
        collector.collect('sth', 'else')

        then:
        collector.results.size() == 3

        and: "save to file"
        resultsFile.size() == 0

        and:
        collector.saveResults(resultsFile.toString())

        and:
        resultsFile.size() == 20
    }

}
