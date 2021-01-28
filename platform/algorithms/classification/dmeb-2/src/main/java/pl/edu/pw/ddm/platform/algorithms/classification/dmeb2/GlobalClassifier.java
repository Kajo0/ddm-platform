package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2;

import static java.util.Comparator.naturalOrder;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import lombok.NoArgsConstructor;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.LabeledObservation;
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
public class GlobalClassifier implements Classifier {

    private SVMModel firstLevelClassifier;
    private LocalRepresentativesModel[] secondLevelClassifiers;
    private Map<Integer, LinearNNSearch> knnModelMap;

    public GlobalClassifier(SVMModel firstLevelClassifier,
                                                LocalRepresentativesModel[] secondLevelClassifiers,
                                                Map<Integer, LinearNNSearch> knnModelMap) {
        this.firstLevelClassifier = firstLevelClassifier;
        this.secondLevelClassifiers = secondLevelClassifiers;
        this.knnModelMap = knnModelMap;
    }

    @Override
    public void classify(SampleProvider sampleProvider, ParamProvider paramProvider, ResultCollector resultCollector) {
        if (DMeb2.useLocalClassifier(paramProvider)) {
            List<SVMModel> localClassifier = Collections.singletonList(findLocalClassifier());
            sampleProvider.forEachRemaining(sample -> resultCollector.collect(
                    sample.getId(),
                    majorityVoting(
                            sample.getNumericAttributes(),
                            localClassifier)));
        } else {
            int knnParam = paramProvider.provideNumeric("knn_k", 3d).intValue();
            if (knnParam == -1) {
                knnParam = Math.abs(2 * secondLevelClassifiers.length);
                System.out.println("  [[FUTURE LOG]] knn param set to=" + knnParam);
            }
            boolean firstLevelOnly = DMeb2.firstLevelOnly(paramProvider);
            int knnParamf = knnParam;
            sampleProvider.forEachRemaining(sample -> resultCollector.collect(sample.getId(),
                    String.valueOf(classify(sample.getNumericAttributes(), knnParamf, firstLevelOnly))));
        }
    }

    private SVMModel findLocalClassifier() {
        LabeledObservation localhostHash = LocalRepresentativesModel.dummyObservation();
        if (LocalRepresentativesModel.CANNOT_LOCALHOST == localhostHash.getIndex()) {
            System.err.println("  [[FUTURE LOG]] Cannot InetAddress.getLocalHost()");
            throw new RuntimeException("Cannot InetAddress.getLocalHost(): CANNOT_LOCALHOST");
        }

        System.out.println("  [[FUTURE LOG]] Searching for classifier with dummy ID: " + localhostHash.getIndex());
        SVMModel model = Stream.of(secondLevelClassifiers)
                .filter(cl -> cl.getRepresentativeList()
                        .stream()
                        .filter(o -> LocalRepresentativesModel.DUMMY_TARGET == o.getTarget())
                        .anyMatch(o -> localhostHash.getIndex() == o.getIndex()))
                .findFirst()
                .map(LocalRepresentativesModel::getSvmModel)
                .orElseThrow(() -> new RuntimeException("Cannot find local classifier with hash ID " + localhostHash.getIndex()));
        System.out.println("  [[FUTURE LOG]] Found and using classifier with dummy ID: " + localhostHash.getIndex());
        return model;
    }

    private int classify(double[] features, int knnParam, boolean firstLevelOnly) {
        try {
            int predictedByFirstLevelClassifierLabel = firstLevelClassifier.classify(features);
            if (firstLevelOnly) {
                return predictedByFirstLevelClassifierLabel;
            }

            LinearNNSearch knn = knnModelMap.get(predictedByFirstLevelClassifierLabel);
            if (knn == null) {
                // should never be here, it may be cause of empty train set when random_percent = 0 for separated data
                return predictedByFirstLevelClassifierLabel;
            }
            Instances instances = knn.kNearestNeighbours(WekaUtils.toInstance(features), knnParam);
            List<SVMModel> svmModels = new ArrayList<>();
            for (Instance i : instances) {
                LocalRepresentativesModel secondLevelClassifier = secondLevelClassifiers[(int) i.classValue()];
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
