package pl.edu.pw.ddm.platform.metrics

import pl.edu.pw.ddm.platform.metrics.dto.MetricsSummary
import spock.lang.Specification

class DdmPrecisionSpec extends Specification {

    def "should calculate perfect precision for binary class"() {
        given:
        def data = new SortedData() {

            @Override
            List<String> predicationLabels() {
                return ['0', '1', '0', '1', '1']
            }

            @Override
            List<String> realLabels() {
                return ['0', '1', '0', '1', '1']
            }
        }
        def summary = new MetricsSummary()

        when:
        def result = new DdmPrecision().calculate(data, summary)

        then:
        result == 1
    }

    def "should calculate precision for binary class"() {
        given:
        def data = new SortedData() {

            @Override
            List<String> predicationLabels() {
                return ['0', '1', '0', '1', '0', '1', '0', '1']
            }

            @Override
            List<String> realLabels() {
                return ['0', '1', '0', '1', '0', '1', '1', '0']
            }
        }
        def summary = new MetricsSummary()

        when:
        def result = new DdmPrecision().calculate(data, summary)

        then:
        result == 0.75
    }

    def "should calculate perfect precision for multi class"() {
        given:
        def data = new SortedData() {

            @Override
            List<String> predicationLabels() {
                return ['0', '1', '0', '1', '2']
            }

            @Override
            List<String> realLabels() {
                return ['0', '1', '0', '1', '2']
            }
        }
        def summary = new MetricsSummary()

        when:
        def result = new DdmPrecision().calculate(data, summary)

        then:
        result == 1
    }

    // test-case according to https://towardsdatascience.com/multi-class-metrics-made-simple-part-i-precision-and-recall-9250280bddc2
    def "should calculate precision for multi class"() {
        given:
        def data = new SortedData() {

            @Override
            List<String> predicationLabels() {
                return ['C', 'C', 'C', 'C', 'H', 'F', 'C', 'C', 'C', 'C', 'C', 'C', 'H', 'H', 'F', 'F', 'C', 'C', 'C', 'H', 'H', 'H', 'H', 'H', 'H']
            }

            @Override
            List<String> realLabels() {
                return ['C', 'C', 'C', 'C', 'C', 'C', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'H', 'H', 'H', 'H', 'H', 'H', 'H', 'H', 'H']
            }
        }
        def summary = new MetricsSummary()

        when:
        def result = new DdmPrecision().calculate(data, summary)

        then:
        result == 0.547008547008547
    }

}
