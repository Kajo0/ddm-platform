package pl.edu.pw.ddm.platform.metrics.util

import spock.lang.Specification

class ConversionUtilsSpec extends Specification {

    def "should map string values to ints started from 0"() {
        when:
        def ints = ConversionUtils.mapToInts(labels, predictions)

        then:
        ints[0] == resultLabels
        ints[1] == resultPredictions

        where:
        labels                                                     | predictions                                                || resultLabels             | resultPredictions
        ['iris', 'sentosa', 'iris', 'albis', 'hey', 'iris', 'hey'] | ['iris', 'sentosa', 'iris', 'albis', 'hey', 'iris', 'hey'] || [0, 1, 0, 2, 3, 0, 3]    | [0, 1, 0, 2, 3, 0, 3]
        ['iris', 'sentosa', 'iris', 'albis', 'hey', 'iris', 'hey'] | ['new', 'sentosa', 'iris', 'albis', 'hey', 'iris', 'hey']  || [0, 1, 0, 2, 3, 0, 3]    | [4, 1, 0, 2, 3, 0, 3]
        ['a', 'b', 'c', 'c', 'c', 'd', 'e', 'f']                   | ['g', 'h', 'i', 'j', 'f', 'k', 'l', 'm']                   || [0, 1, 2, 2, 2, 3, 4, 5] | [6, 7, 8, 9, 5, 10, 11, 12]
        ['a', 'a', 'a', 'a']                                       | ['b', 'b', 'b', 'b']                                       || [0, 0, 0, 0]             | [1, 1, 1, 1]
        []                                                         | []                                                         || []                       | []
    }

}
