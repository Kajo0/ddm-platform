package pl.edu.pw.ddm.platform.strategies.conceptshift

import spock.lang.Specification

class DataShifterSpec extends Specification {

    def "should shift labels according to the arguments by adding prefixes"() {
        given:
        def shifter = new DataShifter(workers, shifts, 'BoGy', data, new Random(10))

        when:
        def result = shifter.shift()

        then:
        result.size() == data.size()

        and:
        def map = result.groupBy { it.value.node }
        def prefixed = map.sort { it.key }
                .collect {
                    it.value
                            .values()
                            .count { it.label.startsWith(DataShifter.SHIFT_PREFIX) }
                }
        prefixed == prefixedCounts

        where:
        workers | shifts | data                              || prefixedCounts
        2       | 1      | (1..10).collect { it as String }  || [5, 0]
        8       | 1      | (1..100).collect { it as String } || [[12], (2..8).collect { 0 }].flatten()
        8       | 3      | (1..100).collect { it as String } || [[12, 12, 12], (4..8).collect { 0 }].flatten()
        8       | 7      | (1..100).collect { it as String } || [(1..7).collect { 12 }, [0]].flatten()
    }

    def "should prepare shifted label"() {
        given:
        def shifter = new DataShifter(0, 0, label, null, null)

        expect:
        shifter.shiftLabel(shift) == shiftedLabel

        where:
        label  | shift || shiftedLabel
        'a'    | 0     || '80000a'
        'a'    | 1     || '80001a'
        'a'    | 2     || '80002a'
        'a'    | 3     || '80003a'
        '1'    | 0     || '800001'
        '1'    | 10    || '8000101'
        '1'    | -10   || '8000-101'
        'baca' | -10   || '8000-10baca'
    }

}
