package pl.edu.pw.ddm.platform.algorithm.classification.naivebayes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class GModel implements GlobalModel {

    private final Map<String, List<StatisticalSummaryValues>> labelColStats = new HashMap<>();
    private final Map<String, Double> priorProbability = new HashMap<>();
    private final Set<String> labels = new HashSet<>();

    public StatisticalSummaryValues getLabelCol(String label, int col) {
        List<StatisticalSummaryValues> stats = labelColStats.get(label);
        if (stats != null) {
            return stats.get(col);
        } else {
            return null;
        }
    }

    public void addPriorProbability(String label, double probability) {
        labels.add(label);
        priorProbability.put(label, probability);
    }

    public double getPriorProbability(String label) {
        return priorProbability.getOrDefault(label, 0d);
    }

}
