package pl.edu.pw.ddm.platform.strategies.mostof

import spock.lang.Specification

class ScatteringAmountsPreparerSpec extends Specification {

    def "should prepare scattering amounts when labels < workers with no additional classes"() {
        given:
        def labelScattering = new ScatteringLabelsPreparer(4, 3, 0).prepare() // FIXME it assumes that works
        def labelCount = ['A': 1000L,
                          'B': 1000L,
                          'C': 1000L]
        def preparer = new ScatteringAmountsPreparer(labelCount, labelScattering, 0.8, 0.05)

        when:
        def scattering = preparer.prepare()

        then:
        scattering.mainMap[0].map['A'].intValue() == 800

        scattering.mainMap[1].map['B'].intValue() == 800

        scattering.mainMap[2].map['C'].intValue() == 800

        scattering.mainMap[3].map['A'].intValue() == 200
        scattering.mainMap[3].map['B'].intValue() == 200
        scattering.mainMap[3].map['C'].intValue() == 200
    }

    def "should prepare scattering amounts when labels < workers with additional classes"() {
        given:
        def labelScattering = new ScatteringLabelsPreparer(4, 3, 2).prepare() // FIXME it assumes that works
        def labelCount = ['A': 1000L,
                          'B': 1000L,
                          'C': 1000L]
        def preparer = new ScatteringAmountsPreparer(labelCount, labelScattering, 0.8, 0.05)

        when:
        def scattering = preparer.prepare()

        then:
        scattering.mainMap[0].map['A'].intValue() == 720
        scattering.mainMap[0].map['B'].intValue() == 40
        scattering.mainMap[0].map['C'].intValue() == 40

        scattering.mainMap[1].map['B'].intValue() == 720
        scattering.mainMap[1].map['A'].intValue() == 40
        scattering.mainMap[1].map['C'].intValue() == 40

        scattering.mainMap[2].map['C'].intValue() == 720
        scattering.mainMap[2].map['A'].intValue() == 40
        scattering.mainMap[2].map['B'].intValue() == 40

        scattering.mainMap[3].map['A'].intValue() == 200
        scattering.mainMap[3].map['B'].intValue() == 200
        scattering.mainMap[3].map['C'].intValue() == 200
    }

    def "should prepare scattering amounts when labels == workers with one additional class"() {
        given:
        def labelScattering = new ScatteringLabelsPreparer(3, 3, 1).prepare() // FIXME it assumes that works
        def labelCount = ['A': 1000L,
                          'B': 1000L,
                          'C': 1000L]
        def preparer = new ScatteringAmountsPreparer(labelCount, labelScattering, 0.8, 0.05)

        when:
        def scattering = preparer.prepare()

        then:
        scattering.mainMap[0].map['A'].intValue() == 950
        scattering.mainMap[0].map['B'].intValue() == 50

        scattering.mainMap[1].map['B'].intValue() == 950
        scattering.mainMap[1].map['C'].intValue() == 50

        scattering.mainMap[2].map['C'].intValue() == 950
        scattering.mainMap[2].map['A'].intValue() == 50
    }

    def "should prepare scattering amounts when labels > workers with no additional classes"() {
        given:
        def labelScattering = new ScatteringLabelsPreparer(3, 5, 0).prepare() // FIXME it assumes that works
        def labelCount = ['A': 1000L,
                          'B': 1000L,
                          'C': 1000L,
                          'D': 1000L,
                          'E': 1000L]
        def preparer = new ScatteringAmountsPreparer(labelCount, labelScattering, 0.8, 0.05)

        when:
        def scattering = preparer.prepare()

        then:
        scattering.mainMap[0].map['A'].intValue() == 1000
        scattering.mainMap[0].map['D'].intValue() == 1000

        scattering.mainMap[1].map['B'].intValue() == 1000
        scattering.mainMap[1].map['E'].intValue() == 1000

        scattering.mainMap[2].map['C'].intValue() == 1000
    }

    def "should prepare scattering amounts when labels > workers with four or more additional classes"() {
        given:
        def labelScattering = new ScatteringLabelsPreparer(3, 5, additional).prepare() // FIXME it assumes that works
        def labelCount = ['A': 1000L,
                          'B': 1000L,
                          'C': 1000L,
                          'D': 1000L,
                          'E': 1000L]
        def preparer = new ScatteringAmountsPreparer(labelCount, labelScattering, 0.8, 0.1)

        when:
        def scattering = preparer.prepare()

        then:
        scattering.mainMap[0].map['A'].intValue() == 800
        scattering.mainMap[0].map['D'].intValue() == 800
        scattering.mainMap[0].map['B'].intValue() == 100
        scattering.mainMap[0].map['C'].intValue() == 100
        scattering.mainMap[0].map['E'].intValue() == 100

        scattering.mainMap[1].map['B'].intValue() == 800
        scattering.mainMap[1].map['E'].intValue() == 800
        scattering.mainMap[1].map['A'].intValue() == 100
        scattering.mainMap[1].map['C'].intValue() == 100
        scattering.mainMap[1].map['D'].intValue() == 100

        scattering.mainMap[2].map['C'].intValue() == 800
        scattering.mainMap[2].map['A'].intValue() == 100
        scattering.mainMap[2].map['B'].intValue() == 100
        scattering.mainMap[2].map['D'].intValue() == 100
        scattering.mainMap[2].map['E'].intValue() == 100

        where:
        additional << [4, 5, 6]
    }

}
