package pl.edu.pw.ddm.platform.strategies.mostof

import spock.lang.Specification

class ScatteringLabelsPreparerSpec extends Specification {

    def "should prepare scattering when labels < workers with no additional classes"() {
        given:
        def preparer = new ScatteringLabelsPreparer(4, 3, 0)

        when:
        def scattering = preparer.prepare()

        then:
        scattering.full.map == [0: [0] as Set,
                                1: [1] as Set,
                                2: [2] as Set]
        scattering.empty.map == [3: [0, 1, 2] as Set]
        scattering.additional.map == [:]
    }

    def "should prepare scattering when labels < workers with additional classes"() {
        given:
        def preparer = new ScatteringLabelsPreparer(4, 3, 2)

        when:
        def scattering = preparer.prepare()

        then:
        scattering.full.map == [0: [0] as Set,
                                1: [1] as Set,
                                2: [2] as Set]
        scattering.empty.map == [3: [0, 1, 2] as Set]
        scattering.additional.map == [0: [1, 2] as Set,
                                      1: [0, 2] as Set,
                                      2: [0, 1] as Set]
    }

    def "should prepare scattering when labels == workers with one additional class"() {
        given:
        def preparer = new ScatteringLabelsPreparer(3, 3, 1)

        when:
        def scattering = preparer.prepare()

        then:
        scattering.full.map == [0: [0] as Set,
                                1: [1] as Set,
                                2: [2] as Set]
        scattering.empty.map == [:]
        scattering.additional.map == [0: [1] as Set,
                                      1: [2] as Set,
                                      2: [0] as Set]
    }

    def "should prepare scattering when labels > workers with no additional classes"() {
        given:
        def preparer = new ScatteringLabelsPreparer(3, 5, 0)

        when:
        def scattering = preparer.prepare()

        then:
        scattering.full.map == [0: [0, 3] as Set,
                                1: [1, 4] as Set,
                                2: [2] as Set]
        scattering.empty.map == [:]
        scattering.additional.map == [:]
    }

    def "should prepare scattering when labels > workers with four or more additional classes"() {
        given:
        def preparer = new ScatteringLabelsPreparer(3, 5, additional)

        when:
        def scattering = preparer.prepare()

        then:
        scattering.full.map == [0: [0, 3] as Set,
                                1: [1, 4] as Set,
                                2: [2] as Set]
        scattering.empty.map == [:]
        scattering.additional.map == [0: [1, 2, 4] as Set,
                                      1: [0, 2, 3] as Set,
                                      2: [0, 1, 3, 4] as Set]

        where:
        additional << [4, 5, 6]
    }

}
