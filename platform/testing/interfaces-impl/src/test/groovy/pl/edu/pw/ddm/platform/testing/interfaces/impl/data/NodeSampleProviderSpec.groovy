package pl.edu.pw.ddm.platform.testing.interfaces.impl.data

import spock.lang.Specification

class NodeSampleProviderSpec extends Specification {

    def "should provide given samples"() {
        when:
        def provider = new NodeSampleProvider(
                [
                        new NodeData('1', 'l1', ['1', '2', '3'] as String[], ['numeric', 'numeric', 'numeric'] as String[]),
                        new NodeData('3', 'l3', ['111', '222', '333'] as String[], ['numeric', 'numeric', 'numeric'] as String[]),
                        new NodeData('2', 'l2', ['11', '22', '33'] as String[], ['numeric', 'numeric', 'numeric'] as String[]),
                        new NodeData('4', 'l4', ['1111', '2222', '3333'] as String[], ['numeric', 'numeric', 'numeric'] as String[])
                ]
        )

        then:
        provider.all().size() == 4

        and:
        provider.hasNext()
        provider.next().id == '1'
        provider.hasNext()
        provider.next().id == '3'
        provider.hasNext()
        provider.next().id == '2'
        provider.hasNext()
        provider.next().id == '4'

        and:
        !provider.hasNext()
    }

}
