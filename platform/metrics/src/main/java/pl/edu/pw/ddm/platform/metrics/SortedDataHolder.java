package pl.edu.pw.ddm.platform.metrics;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import pl.edu.pw.ddm.platform.metrics.dto.IdLabel;

class SortedDataHolder implements SortedData {

    private final List<String> predictions;
    private final List<String> expectations;

    SortedDataHolder(List<IdLabel> predictionResults, List<IdLabel> expectedResults) {
        this.predictions = predictionResults.stream()
                .sorted(Comparator.comparing(IdLabel::getId))
                .map(IdLabel::getLabel)
                .collect(Collectors.toList());
        this.expectations = expectedResults.stream()
                .sorted(Comparator.comparing(IdLabel::getId))
                .map(IdLabel::getLabel)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> predicationLabels() {
        return predictions;
    }

    @Override
    public List<String> realLabels() {
        return expectations;
    }

}
