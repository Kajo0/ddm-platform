package pl.edu.pw.ddm.platform.testing.interfaces.impl.data

import pl.edu.pw.ddm.platform.interfaces.data.DataProvider
import spock.lang.Specification

class AttributeTypeDetectorSpec extends Specification {

    def "should detect column types"() {
        given:
        def desc = DataProvider.DataDesc.builder()
                .separator(',')
                .attributesAmount(5)
                .build()
        def detector = new AttributeTypeDetector(path, desc)

        when:
        def result = detector.detect();

        then:
        result == types

        where:
        path                                                || types
        resourcePath('/numeric5cols.data')                  || ['numeric', 'numeric', 'numeric', 'numeric', 'numeric'] as String[]
        resourcePath('/numeric3nominal2cols.data')          || ['numeric', 'nominal', 'numeric', 'numeric', 'nominal'] as String[]
        resourcePath('/firstLineNumericSecondNominal.data') || ['nominal', 'nominal', 'nominal', 'nominal', 'nominal'] as String[]
    }

    def resourcePath(def resource) {
        getClass().getResource(resource).path
    }

}
