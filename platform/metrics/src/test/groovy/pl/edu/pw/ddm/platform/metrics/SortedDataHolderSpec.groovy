package pl.edu.pw.ddm.platform.metrics

import pl.edu.pw.ddm.platform.metrics.dto.IdLabel
import spock.lang.Specification

class SortedDataHolderSpec extends Specification {

    def "should sort input data by id"() {
        given:
        def predictions = [IdLabel.of('2', '1'), IdLabel.of('7', '3'), IdLabel.of('1', '2')]
        def expectations = [IdLabel.of('4', '2'), IdLabel.of('1', '6'), IdLabel.of('10', 'non-numeric-but-char-sort')]

        when:
        def holder = new SortedDataHolder(predictions, expectations)

        then:
        holder.predicationLabels() == ['2', '1', '3']
        holder.realLabels() == ['6', 'non-numeric-but-char-sort', '2']
    }

}
