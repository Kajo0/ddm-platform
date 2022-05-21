package ddm.samples

import pl.edu.pw.ddm.platform.distfunc.EuclideanDistance
import spock.lang.Specification

import static pl.edu.pw.ddm.platform.interfaces.data.DataProvider.AttributeType.NUMERIC

class MoreSophisticatedDenseExtractorSpec extends Specification {

    def "should split line by more sophisticated dense and outliers"() {
        given:
        def i = 0;
        def data = [
                new NodeData(i++ as String, 'A', ['10'] as String[], [NUMERIC] as String[]),
                new NodeData(i++ as String, 'A', ['20'] as String[], [NUMERIC] as String[]),
                new NodeData(i++ as String, 'A', ['30'] as String[], [NUMERIC] as String[]),
                new NodeData(i++ as String, 'A', ['49'] as String[], [NUMERIC] as String[]),
                new NodeData(i++ as String, 'A', ['50'] as String[], [NUMERIC] as String[]),
                new NodeData(i++ as String, 'A', ['50'] as String[], [NUMERIC] as String[]),
                new NodeData(i++ as String, 'A', ['50'] as String[], [NUMERIC] as String[]),
                new NodeData(i++ as String, 'A', ['51'] as String[], [NUMERIC] as String[]),
                new NodeData(i++ as String, 'A', ['70'] as String[], [NUMERIC] as String[]),
                new NodeData(i++ as String, 'A', ['80'] as String[], [NUMERIC] as String[]),
                new NodeData(i++ as String, 'A', ['90'] as String[], [NUMERIC] as String[]),
                new NodeData(i++ as String, 'A', ['190'] as String[], [NUMERIC] as String[]),
                new NodeData(i++ as String, 'A', ['191'] as String[], [NUMERIC] as String[])
        ]

        and:
        def extractor = new MoreSophisticatedDenseExtractor(data, new EuclideanDistance(), 0.2, 0.9)

        when:
        def result = extractor.extract()

        then:
        result

        and:
        result.dense.collect { it.id } as Set == ['3', '4', '5', '6', '7', '8', '9', '10'] as Set
        result.outliers.collect { it.id } as Set == ['0', '1', '2', '11', '12'] as Set
//        println 'dense:'
//        result.dense
//                .sort { it.id as int }
//                .each { println "${it.id} - ${it.numericAttributes}" }
//        println 'outliers:'
//        result.outliers
//                .sort { it.id as int }
//                .each { println "${it.id} - ${it.numericAttributes}" }
    }

}
