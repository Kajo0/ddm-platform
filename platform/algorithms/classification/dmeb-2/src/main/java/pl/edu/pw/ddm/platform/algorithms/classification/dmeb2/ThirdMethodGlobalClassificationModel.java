package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2;

import static java.util.Comparator.naturalOrder;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.NoArgsConstructor;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.SVMModel;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.WekaUtils;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ResultCollector;
import pl.edu.pw.ddm.platform.interfaces.data.SampleProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.LinearNNSearch;

@NoArgsConstructor
public class ThirdMethodGlobalClassificationModel implements Classifier {

    private SVMModel firstLevelClassifier;
    private ThirdMethodLocalSVMWithRepresentatives[] secondLevelClassifiers;
    private Map<Integer, LinearNNSearch> knnModelMap;

    public ThirdMethodGlobalClassificationModel(SVMModel firstLevelClassifier,
                                                ThirdMethodLocalSVMWithRepresentatives[] secondLevelClassifiers,
                                                Map<Integer, LinearNNSearch> knnModelMap) {
        this.firstLevelClassifier = firstLevelClassifier;
        this.secondLevelClassifiers = secondLevelClassifiers;
        this.knnModelMap = knnModelMap;
    }

    @Override
    public void classify(SampleProvider sampleProvider, ParamProvider paramProvider, ResultCollector resultCollector) {
        int knnParam = paramProvider.provideNumeric("knn_k", 3d).intValue();
        sampleProvider.forEachRemaining(sample -> resultCollector.collect(sample.getId(), String.valueOf(classify(sample.getNumericAttributes(), knnParam))));
    }

    private int classify(double[] features, int knnParam) {
        try {
            int predictedByFirstLevelClassifierLabel = firstLevelClassifier.classify(features);
            LinearNNSearch knn = knnModelMap.get(predictedByFirstLevelClassifierLabel);
            Instances instances = knn.kNearestNeighbours(WekaUtils.toInstance(features), knnParam);
            List<SVMModel> svmModels = new ArrayList<>();
            for (Instance i : instances) {
                ThirdMethodLocalSVMWithRepresentatives secondLevelClassifier = secondLevelClassifiers[(int) i.classValue()];
                svmModels.add(secondLevelClassifier.getSvmModel());
            }
            return majorityVoting(features, svmModels);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int majorityVoting(double[] features, List<SVMModel> svmModels) {
        Map<Integer, Long> votes = svmModels.stream()
                .map(classificationModel -> classificationModel.classify(features))
                .collect(groupingBy(identity(), counting()));
        long maxVotes = votes.values().stream().max(naturalOrder()).get();
        return votes.entrySet().stream()
                .filter(e -> e.getValue() == maxVotes)
                .findFirst()
                .get()
                .getKey();
    }

    @Override
    public String name() {
        return "D-MEB-2";
    }

}
