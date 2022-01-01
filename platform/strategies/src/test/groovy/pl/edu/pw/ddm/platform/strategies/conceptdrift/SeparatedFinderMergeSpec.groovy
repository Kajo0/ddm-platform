package pl.edu.pw.ddm.platform.strategies.conceptdrift

import org.apache.commons.math3.util.Pair
import spock.lang.Specification

class SeparatedFinderMergeSpec extends Specification {

    def "should merge buckets during find"() {
        given:
        def oneLabelData = [
                '1': new IdValuesPair('1', ['a', 'b'] as String[]),
                '2': new IdValuesPair('2', ['c', 'd'] as String[]),
                '3': new IdValuesPair('3', ['c', 'b'] as String[])
        ]

        and:
        def multiBucket = new MultiBuckets()
        multiBucket.add(0, 'a', ['1'])
        multiBucket.add(0, 'c', ['2', '3'])
        multiBucket.add(1, 'b', ['1', '3'])
        multiBucket.add(1, 'd', ['2'])

        and:
        def finder = new SeparatedFinder(oneLabelData,
                multiBucket,
                2,
                [Pair.create(0, false), Pair.create(1, false)],
                [])

        when:
        finder.findLowestEntropyColumn()
        def separated = finder.find()

        then:
        separated.size() == 1
    }

    def "should merge buckets during find - 2nd case"() {
        given:
        def oneLabelData = [
                '1': new IdValuesPair('1', ['a', 'b'] as String[]),
                '2': new IdValuesPair('2', ['c', 'd'] as String[]),
                '3': new IdValuesPair('3', ['a', 'd'] as String[])
        ]

        and:
        def multiBucket = new MultiBuckets()
        multiBucket.add(0, 'a', ['1', '3'])
        multiBucket.add(0, 'c', ['2'])
        multiBucket.add(1, 'b', ['1'])
        multiBucket.add(1, 'd', ['2', '3'])

        and:
        def finder = new SeparatedFinder(oneLabelData,
                multiBucket,
                2,
                [Pair.create(0, false), Pair.create(1, false)],
                [])

        when:
        finder.findLowestEntropyColumn()
        def separated = finder.find()

        then:
        separated.size() == 1
    }

    def "should not merge buckets during find"() {
        given:
        def oneLabelData = [
                '1': new IdValuesPair('1', ['a', 'b'] as String[]),
                '2': new IdValuesPair('2', ['c', 'd'] as String[]),
                '3': new IdValuesPair('3', ['e', 'f'] as String[])
        ]

        and:
        def multiBucket = new MultiBuckets()
        multiBucket.add(0, 'a', ['1'])
        multiBucket.add(0, 'c', ['2'])
        multiBucket.add(0, 'e', ['3'])
        multiBucket.add(1, 'b', ['1'])
        multiBucket.add(1, 'd', ['2'])
        multiBucket.add(1, 'f', ['3'])

        and:
        def finder = new SeparatedFinder(oneLabelData,
                multiBucket,
                2,
                [Pair.create(0, false), Pair.create(1, false)],
                [])

        when:
        finder.findLowestEntropyColumn()
        def separated = finder.find()

        then:
        separated.size() == 3
    }

    def "should not merge buckets during find - 2nd case"() {
        given:
        def oneLabelData = [
                '1': new IdValuesPair('1', ['a', 'b'] as String[]),
                '2': new IdValuesPair('2', ['c', 'd'] as String[]),
                '3': new IdValuesPair('3', ['e', 'b'] as String[])
        ]

        and:
        def multiBucket = new MultiBuckets()
        multiBucket.add(0, 'a', ['1'])
        multiBucket.add(0, 'c', ['2'])
        multiBucket.add(0, 'e', ['3'])
        multiBucket.add(1, 'b', ['1', '3'])
        multiBucket.add(1, 'd', ['2'])

        and:
        def finder = new SeparatedFinder(oneLabelData,
                multiBucket,
                2,
                [Pair.create(0, false), Pair.create(1, false)],
                [])

        when:
        finder.findLowestEntropyColumn()
        def separated = finder.find()

        then:
        separated.size() == 2
    }

}
