package pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.filterers

import pl.edu.pw.ddm.platform.distfunc.EuclideanDistance
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.NodeData
import spock.lang.Specification

class ConvexHullFilterSpec extends Specification {

    def "should return convex hull of data"() {
        given:
        def types = (1..2).collect { 'numeric' } as String[]
        def data = [
                new NodeData('1', '1', ['-334', '277'] as String[], types),
                new NodeData('2', '1', ['-314', '301'] as String[], types),
                new NodeData('3', '1', ['-263', '302'] as String[], types),
                new NodeData('4', '1', ['-227', '282'] as String[], types),
                new NodeData('5', '1', ['-253', '264'] as String[], types),
                new NodeData('6', '1', ['-291', '255'] as String[], types),
                new NodeData('7', '1', ['-312', '268'] as String[], types),
                new NodeData('8', '1', ['-290', '283'] as String[], types),
                new NodeData('9', '1', ['-254', '270'] as String[], types),
                new NodeData('10', '1', ['-243', '247'] as String[], types),
                new NodeData('11', '1', ['-220', '249'] as String[], types),
                new NodeData('12', '1', ['-220', '252'] as String[], types),
                new NodeData('13', '1', ['-219', '225'] as String[], types),
                new NodeData('14', '1', ['-232', '200'] as String[], types),
                new NodeData('15', '1', ['-285', '198'] as String[], types),
                new NodeData('16', '1', ['-333', '223'] as String[], types),
                new NodeData('17', '1', ['-281', '236'] as String[], types),
                new NodeData('18', '1', ['-264', '222'] as String[], types),
                new NodeData('19', '1', ['-246', '208'] as String[], types),
                new NodeData('20', '1', ['-257', '202'] as String[], types),
                new NodeData('21', '1', ['-298', '215'] as String[], types),
                new NodeData('22', '1', ['-305', '189'] as String[], types),
                new NodeData('23', '1', ['-426', '254'] as String[], types),
                new NodeData('24', '1', ['-372', '338'] as String[], types),
                new NodeData('25', '1', ['-253', '352'] as String[], types),
                new NodeData('26', '1', ['-123', '296'] as String[], types),
                new NodeData('27', '1', ['-135', '187'] as String[], types),
                new NodeData('28', '1', ['-228', '115'] as String[], types),
                new NodeData('29', '1', ['-382', '124'] as String[], types)
        ]

        and:
        def alg = new ConvexHullFilter(new EuclideanDistance(), data)

        when:
        def result = alg.process()

        then:
        result.size() == 7
        (23..29).each { index ->
            assert result.find { it.id == index as String }
        }
    }

    def "should return convex hull of 3d data"() {
        given:
        def types = (1..3).collect { 'numeric' } as String[]
        def data = [
                new NodeData('1', '1', ['-100', '-100', '-100'] as String[], types),
                new NodeData('2', '1', ['-100', '-100', '100'] as String[], types),
                new NodeData('3', '1', ['-100', '100', '-100'] as String[], types),
                new NodeData('4', '1', ['-100', '100', '100'] as String[], types),
                new NodeData('5', '1', ['100', '-100', '-100'] as String[], types),
                new NodeData('6', '1', ['100', '100', '-100'] as String[], types),
                new NodeData('7', '1', ['100', '-100', '100'] as String[], types),
                new NodeData('8', '1', ['100', '100', '100'] as String[], types),
                new NodeData('9', '1', ['0', '0', '0'] as String[], types),
                new NodeData('10', '1', ['0', '50', '0'] as String[], types),
                new NodeData('11', '1', ['99', '99', '99'] as String[], types),
                new NodeData('12', '1', ['-99', '90', '99'] as String[], types),
                new NodeData('13', '1', ['0', '1', '54'] as String[], types),
                new NodeData('14', '1', ['-10', '-20', '40'] as String[], types),
                new NodeData('15', '1', ['11', '10', '90'] as String[], types),
                new NodeData('16', '1', ['1', '-70', '7'] as String[], types),
                new NodeData('17', '1', ['-92', '70', '-44'] as String[], types)
        ]

        and:
        def alg = new ConvexHullFilter(new EuclideanDistance(), data)

        when:
        def result = alg.process()

        then:
        result.forEach { println it }

        and:
        result.size() == 8
        (1..8).each { index ->
            assert result.find { it.id == index as String }
        }
    }
}
