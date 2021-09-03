package pl.edu.pw.ddm.platform.algorithm.classification.naivebayes;

import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import pl.edu.pw.ddm.platform.interfaces.data.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
class StatsCollector {

    private final Collection<Data> data;
    private final int attributesAmount;

    LModel collect() {
        LModel model = new LModel(attributesAmount);

        data.forEach(d -> {
            List<SummaryStatistics> stats = model.getLabelColStats()
                    .computeIfAbsent(d.getLabel(), s -> prepareStats());

            for (int i = 0; i < StatsCollector.this.attributesAmount; ++i) {
                stats.get(i).addValue(d.getNumericAttributes()[i]);
            }

            model.addLabel(d.getLabel());
        });

        return model;
    }

    private List<SummaryStatistics> prepareStats() {
        List<SummaryStatistics> stats = new ArrayList<>(attributesAmount);
        for (int i = 0; i < attributesAmount; ++i) {
            stats.add(new SummaryStatistics());
        }
        return stats;
    }

}
