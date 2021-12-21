package pl.edu.pw.ddm.platform.algorithm.classification.naivebayes;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ResultCollector;
import pl.edu.pw.ddm.platform.interfaces.data.SampleData;
import pl.edu.pw.ddm.platform.interfaces.data.SampleProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.Classifier;

@NoArgsConstructor
@AllArgsConstructor
public class GlobalClassifier implements Classifier {

    private GModel model;

    @Override
    public void classify(SampleProvider sampleProvider, ParamProvider paramProvider, ResultCollector resultCollector) {
        // TODO add other distributions
        GaussianDistribution distribution = new GaussianDistribution();
        sampleProvider.forEachRemaining(s -> resultCollector.collect(s.getId(), classify(s, distribution)));
    }

    private String classify(SampleData sample, Distribution distribution) {
        String resultLabel = "?";
        double max = 0;
        for (String label : model.getLabels()) {
            double val = model.getPriorProbability(label) * colsProbability(label, sample, distribution);
            if (val > max) {
                resultLabel = label;
                max = val;
            }
        }
        return resultLabel;
    }

    private double colsProbability(String label, SampleData sample, Distribution distribution) {
        double result = 1;
        double[] attrs = sample.getNumericAttributes();
        for (int i = 0; i < attrs.length; ++i) {
            StatisticalSummaryValues stats = model.getLabelCol(label, i);
            result *= distribution.probability(attrs[i],
                    stats.getMean(),
                    stats.getStandardDeviation());
        }

        return result;
    }

    @Override
    public String name() {
        return "Naive Bayes";
    }

}
