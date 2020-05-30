package pl.edu.pw.ddm.platform.algorithms.clustering.lct

import spock.lang.Specification

class ClusterMergerSpec extends Specification {

    def "should check if clusters should be merged"() {
        given:
        def first = LModel.LocalCluster.of(
                [1] as double[],
                1,
                firstVariance
        )
        def second = LModel.LocalCluster.of(
                [1] as double[],
                1,
                secondVariance
        )

        expect:
        ClusterMerger.shouldMerge(first, second) == shouldMerge

        where:
        firstVariance | secondVariance || shouldMerge
        1             | 1              || false
        2             | 2              || false
        1             | 2              || true
        2             | 1              || true
    }

    def "should merge two same clusters to almost the same one"() {
        given:
        def first = LModel.LocalCluster.of(
                [2.7, 3.8, 7.2] as double[],
                10,
                0.3
        )
        def second = LModel.LocalCluster.of(
                [2.7, 3.8, 7.2] as double[],
                10,
                0.3
        )


        when:
        def result = ClusterMerger.merge(first, second)

        then:
        with(result) {
            centroid == [2.7, 3.8, 7.2] as double[]
            size == 20
            variance == 0.6d
        }
    }

    def "should merge two clusters to the new one"() {
        given:
        def first = LModel.LocalCluster.of(
                [2.7, 3.8, 7.2] as double[],
                5,
                0.3
        )
        def second = LModel.LocalCluster.of(
                [5.3, 4.2, 6.8] as double[],
                15,
                0.7
        )

        when:
        def result = ClusterMerger.merge(first, second)

        then:
        with(result) {
            centroid == [4.6499999999999995, 4.1000000000000005, 6.8999999999999995] as double[]
            size == 20
            variance == 10.978101021737553d
        }
    }

}
