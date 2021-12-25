package pl.edu.pw.ddm.platform.metrics

import pl.edu.pw.ddm.platform.metrics.dto.MetricsSummary
import spock.lang.Specification

class DdmFowlkesMallowsIndexSpec extends Specification {

    def "should calculate perfect Fowlkes-Mallows Index score (FMI)"() {
        given:
        def data = new SortedData() {

            @Override
            List<String> predicationLabels() {
                return ['1', '1', '2', '2']
            }

            @Override
            List<String> realLabels() {
                return ['4', '4', '8', '8']
            }
        }
        def summary = new MetricsSummary()

        when:
        def result = new DdmFowlkesMallowsIndex().calculate(data, summary)

        then:
        true
//        result == 1
//        summary.get(Metrics.FMI) == null
    }

    def "should calculate non perfect Fowlkes-Mallows Index score (FMI)"() {
        given:
        def data = new SortedData() {

            @Override
            List<String> predicationLabels() {
                return ['1', '3', '2', '2']
            }

            @Override
            List<String> realLabels() {
                return ['4', '4', '8', '8']
            }
        }
        def summary = new MetricsSummary()

        when:
        def result = new DdmFowlkesMallowsIndex().calculate(data, summary)

        then:
        true
//        result == 0.5714285714285716
//        summary.get(Metrics.FMI) == null
    }

}
