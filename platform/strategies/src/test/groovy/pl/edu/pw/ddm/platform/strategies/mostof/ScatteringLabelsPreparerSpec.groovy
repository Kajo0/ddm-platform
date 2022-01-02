package pl.edu.pw.ddm.platform.strategies.mostof

import spock.lang.Specification

class ScatteringLabelsPreparerSpec extends Specification {

    def "should prepare scattering when labels < workers with no additional classes"() {
        given:
        def preparer = new ScatteringLabelsPreparer(4, 3, 0, 0)

        when:
        def scattering = preparer.prepare()

        then:
        scattering.full.map == [0: [0] as Set,
                                1: [1] as Set,
                                2: [2] as Set]
        scattering.empty.map == [3: [0, 1, 2] as Set]
        scattering.additional.map == [:]
    }

    def "should prepare scattering when labels < workers with no additional classes and fill empty with one separated labels"() {
        given:
        def preparer = new ScatteringLabelsPreparer(5, 2, 0, 1)

        when:
        def scattering = preparer.prepare()

        then:
        scattering.full.map == [0: [0] as Set,
                                1: [1] as Set]
        scattering.empty.map == [2: [0] as Set,
                                 3: [1] as Set,
                                 4: [0] as Set]
        scattering.additional.map == [:]
    }

    def "should prepare scattering when labels < workers with no additional classes and fill empty with multiple separated labels"() {
        given:
        def preparer = new ScatteringLabelsPreparer(9, 4, 0, 3)

        when:
        def scattering = preparer.prepare()

        then:
        scattering.full.map == [0: [0] as Set,
                                1: [1] as Set,
                                2: [2] as Set,
                                3: [3] as Set]
        scattering.empty.map == [4: [0, 1, 2] as Set,
                                 5: [3, 0, 1] as Set,
                                 6: [2, 3, 0] as Set,
                                 7: [1, 2, 3] as Set,
                                 8: [0, 1, 2] as Set]
        scattering.additional.map == [:]
    }

    def "should prepare scattering when labels < workers with no additional classes and fill empty with all but separated classes"() {
        given:
        def preparer = new ScatteringLabelsPreparer(5, 2, 0, 200)

        when:
        def scattering = preparer.prepare()

        then:
        scattering.full.map == [0: [0] as Set,
                                1: [1] as Set]
        scattering.empty.map == [2: [0, 1] as Set,
                                 3: [0, 1] as Set,
                                 4: [0, 1] as Set]
        scattering.additional.map == [:]
    }

    def "should prepare scattering when labels < workers with additional classes"() {
        given:
        def preparer = new ScatteringLabelsPreparer(4, 3, 2, 0)

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
        def preparer = new ScatteringLabelsPreparer(3, 3, 1, 0)

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
        def preparer = new ScatteringLabelsPreparer(3, 5, 0, 0)

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
        def preparer = new ScatteringLabelsPreparer(3, 5, additional, 0)

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

    def "should prepare scattering when labels > workers with negative additional classes"() {
        when:
        def scattering = new ScatteringLabelsPreparer(3, 9, -1, 0).prepare()

        then:
        scattering.full.map == [0: [0, 3, 6] as Set,
                                1: [1, 4, 7] as Set,
                                2: [2, 5, 8] as Set]
        scattering.empty.map == [:]
        scattering.additional.map == [0: [1, 2, 4, 5, 7] as Set,
                                      1: [0, 2, 3, 5, 8] as Set,
                                      2: [0, 1, 3, 6, 7] as Set]

        when:
        scattering = new ScatteringLabelsPreparer(3, 9, -5, 0).prepare()

        then:
        scattering.full.map == [0: [0, 3, 6] as Set,
                                1: [1, 4, 7] as Set,
                                2: [2, 5, 8] as Set]
        scattering.empty.map == [:]
        scattering.additional.map == [0: [1] as Set,
                                      1: [2] as Set,
                                      2: [3] as Set]

        when:
        scattering = new ScatteringLabelsPreparer(3, 9, -6, 0).prepare()

        then:
        scattering.full.map == [0: [0, 3, 6] as Set,
                                1: [1, 4, 7] as Set,
                                2: [2, 5, 8] as Set]
        scattering.empty.map == [:]
        scattering.additional.map == [:]
    }

    def "should prepare scattering when labels < workers with negative additional classes"() {
        when:
        def scattering = new ScatteringLabelsPreparer(5, 3, -1, 0).prepare()

        then:
        scattering.full.map == [0: [0] as Set,
                                1: [1] as Set,
                                2: [2] as Set]
        scattering.empty.map == [3: [0, 1, 2] as Set,
                                 4: [0, 1, 2] as Set]
        scattering.additional.map == [0: [1] as Set,
                                      1: [2] as Set,
                                      2: [0] as Set]

        when:
        scattering = new ScatteringLabelsPreparer(5, 3, -2, 0).prepare()

        then:
        scattering.full.map == [0: [0] as Set,
                                1: [1] as Set,
                                2: [2] as Set]
        scattering.empty.map == [3: [0, 1, 2] as Set,
                                 4: [0, 1, 2] as Set]
        scattering.additional.map == [:]
    }

}
