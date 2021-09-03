package pl.edu.pw.ddm.platform.algorithm.classification.naivebayes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class LModel implements LocalModel {

    private final static SummaryStatistics EMPTY = new SummaryStatistics();

    private final Map<String, List<SummaryStatistics>> labelColStats = new HashMap<>();
    private final Map<String, Integer> labelCount = new HashMap<>();
    private final int attributesAmount;

    SummaryStatistics getLabelCol(String label, int col) {
        List<SummaryStatistics> stats = labelColStats.get(label);
        if (stats == null) {
            return EMPTY;
        } else {
            return stats.get(col);
        }
    }

    void addLabel(String label) {
        int count = labelCount.computeIfAbsent(label, s -> 0);
        labelCount.put(label, ++count);
    }

}
