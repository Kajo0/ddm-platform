package pl.edu.pw.ddm.platform.metrics


import pl.edu.pw.ddm.platform.metrics.dto.MetricsSummary
import spock.lang.Specification

class DdmAdjustedRandIndexSpec extends Specification {

    def "should calculate perfect Adjusted Rand Index"() {
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
        def result = new DdmAdjustedRandIndex().calculate(data, summary)

        then:
        result == 1
        summary.get(Metrics.ADJUSTED_RAND_INDEX) == null
    }

    def "should calculate non perfect Adjusted Rand Index"() {
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
        def result = new DdmAdjustedRandIndex().calculate(data, summary)

        then:
        result == 0.5714285714285715
        summary.get(Metrics.ADJUSTED_RAND_INDEX) == null
    }

}