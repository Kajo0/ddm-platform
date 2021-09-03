package pl.edu.pw.ddm.platform.algorithm.classification.naivebayes;

import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.stat.descriptive.AggregateSummaryStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class StatsAggregator {

    private final Collection<LModel> models;
    private final int attributesAmount;

    GModel aggregate() {
        GModel gModel = new GModel();

        Set<String> labels = models.stream()
                .map(LModel::getLabelColStats)
                .map(Map::keySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        for (String label : labels) {
            for (int i = 0; i < attributesAmount; ++i) {
                int finalI = i;
                StatisticalSummaryValues ss = models.stream()
                        .map(m -> m.getLabelCol(label, finalI))
                        .filter(m -> m.getN() > 0)
                        .collect(Collectors.collectingAndThen(Collectors.toList(),
                                AggregateSummaryStatistics::aggregate));

                gModel.getLabelColStats()
                        .computeIfAbsent(label, s -> new ArrayList<>())
                        .add(i, ss);
            }
        }

        calcPriorProbability(gModel);

        return gModel;
    }

    private void calcPriorProbability(GModel gModel) {
        Map<String, AtomicInteger> labelCount = new HashMap<>();
        models.stream()
                .map(LModel::getLabelCount)
                .forEach(lc -> lc.forEach(
                        (label, count) -> labelCount.computeIfAbsent(label, s -> new AtomicInteger(0))
                                .addAndGet(count)));
        int samples = labelCount.values()
                .stream()
                .map(AtomicInteger::intValue)
                .reduce(Integer::sum)
                .orElse(0);

        labelCount.forEach((label, count) -> gModel.addPriorProbability(label, count.doubleValue() / samples));
    }

}
