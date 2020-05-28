package pl.edu.pw.ddm.platform.testing.interfaces.impl.data


import spock.lang.Specification

class NodeParamProviderSpec extends Specification {

    def "should provide given parameters"() {
        given:
        def paramProvider = new NodeParamProvider(
                null,
                [
                        'a': 'b',
                        'c': '1',
                        'd': '2.3',
                        'e': 'f'
                ]
        )

        expect:
        paramProvider.distanceFunction() == null

        and:
        paramProvider.provide('a') == 'b'
        paramProvider.provide('c') == '1'
        paramProvider.provide('d') == '2.3'
        paramProvider.provide('e') == 'f'

        and:
        paramProvider.provideNumeric('c') == 1
        paramProvider.provideNumeric('d') == 2.3

        when:
        paramProvider.provideNumeric('a') == 'b'

        then:
        thrown(NumberFormatException)

        when:
        paramProvider.provideNumeric('e') == 'f'

        then:
        thrown(NumberFormatException)

        and:
        paramProvider.provide('notFound', 'not') == 'not'
        paramProvider.provideNumeric('notFound', 5) == 5
    }

}
