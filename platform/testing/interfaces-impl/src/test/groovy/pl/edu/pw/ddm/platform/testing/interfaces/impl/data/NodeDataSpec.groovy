package pl.edu.pw.ddm.platform.testing.interfaces.impl.data

import org.apache.commons.lang3.NotImplementedException
import spock.lang.Specification

class NodeDataSpec extends Specification {

    def "should test node data methods"() {
        given:
        def data = new NodeData(
                'identifier',
                'label',
                ['1', 'text_2', '3.0', '4', '7'] as String[],
                ['numeric', 'nominal', 'nominal', 'nominal', 'numeric'] as String[]
        )

        expect:
        data.id == 'identifier'
        data.label == 'label'
        data.attributes == ['1', 'text_2', '3.0', '4', '7'] as String[]
        data.colTypes == ['numeric', 'nominal', 'nominal', 'nominal', 'numeric'] as String[]

        when:
        data.numericAttributes

        then:
        thrown(IllegalArgumentException)

        and:
        data.getAttribute(0) == '1'
        data.getAttribute(1) == 'text_2'
        data.getAttribute(2) == '3.0'
        data.getAttribute(3) == '4'
        data.getAttribute(4) == '7'

        and:
        data.getNumericAttribute(0) == 1
        data.getNumericAttribute(4) == 7

        when:
        data.getNumericAttribute(1)

        then:
        thrown(IllegalArgumentException)

        when:
        data.getNumericAttribute(2)

        then:
        thrown(IllegalArgumentException)

        when:
        data.getNumericAttribute(3)

        then:
        thrown(IllegalArgumentException)

        when:
        data.getAttribute('name')

        then:
        thrown(NotImplementedException)

        when:
        data.getNumericAttribute('name')

        then:
        thrown(NotImplementedException)
    }

    def "should test node data numeric methods"() {
        given:
        def data = new NodeData(
                'identifier',
                'label',
                ['1', '3.0', '-4.1'] as String[],
                ['numeric', 'numeric', 'numeric'] as String[]
        )

        expect:
        data.getNumericAttribute(0) == 1
        data.getNumericAttribute(1) == 3.0d
        data.getNumericAttribute(2) == -4.1d

        and:
        data.numericAttributes == [1, 3.0d, -4.1d] as double[]
    }

}
