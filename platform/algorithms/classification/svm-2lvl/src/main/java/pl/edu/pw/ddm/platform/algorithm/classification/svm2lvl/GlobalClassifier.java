package pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl;

import static java.util.Comparator.naturalOrder;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.utils.LabeledObservation;
import pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.utils.SVMModel;
import pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.utils.WekaUtils;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ResultCollector;
import pl.edu.pw.ddm.platform.interfaces.data.SampleProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.LinearNNSearch;

@NoArgsConstructor
@AllArgsConstructor
public class GlobalClassifier implements Classifier {

    private SVMModel globalSvm;
    private LocalRepresentativesModel[] secondLocalModels;
    private Map<Integer, LinearNNSearch> knnModelMap;

    @Override
    public void classify(SampleProvider sampleProvider, ParamProvider paramProvider, ResultCollector resultCollector) {
        if (Svm2Lvl.useLocalClassifier(paramProvider)) {
            List<LocalRepresentativesModel> localClassifier = Collections.singletonList(findLocalClassifier());
            sampleProvider.forEachRemaining(sample -> resultCollector.collect(sample.getId(),
                    majorityVoting(sample.getNumericAttributes(), localClassifier)));
        } else if (Svm2Lvl.firstGlobalLevelOnly(paramProvider)) {
            sampleProvider.forEachRemaining(sample -> resultCollector.collect(sample.getId(),
                    String.valueOf(globalSvm.classify(sample.getNumericAttributes()))));
        } else {
            boolean eachVotes = Boolean.TRUE.toString().equals(paramProvider.provide("each_votes", "false"));
            int knnParam = paramProvider.provideNumeric("knn_k", -1d)
                    .intValue();
            if (knnParam == -1) {
                knnParam = Math.abs(2 * secondLocalModels.length);
                System.out.println("  [[FUTURE LOG]] knn param set to=" + knnParam);
            }
            int knnParamf = knnParam;
            sampleProvider.forEachRemaining(sample -> resultCollector.collect(sample.getId(),
                    String.valueOf(classify(sample.getNumericAttributes(), knnParamf, eachVotes))));
        }
    }

    private LocalRepresentativesModel findLocalClassifier() {
        LabeledObservation localhostHash = LocalRepresentativesModel.dummyObservation();
        if (LocalRepresentativesModel.CANNOT_LOCALHOST == localhostHash.getIndex()) {
            System.err.println("  [[FUTURE LOG]] Cannot InetAddress.getLocalHost()");
            throw new RuntimeException("Cannot InetAddress.getLocalHost(): CANNOT_LOCALHOST");
        }

        System.out.println("  [[FUTURE LOG]] Searching for classifier with dummy ID: " + localhostHash.getIndex());
        LocalRepresentativesModel model = Stream.of(secondLocalModels)
                .filter(cl -> cl.getRepresentatives()
                        .stream()
                        .filter(o -> LocalRepresentativesModel.DUMMY_TARGET == o.getTarget())
                        .anyMatch(o -> localhostHash.getIndex() == o.getIndex()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find local classifier with hash ID " + localhostHash.getIndex()));
        System.out.println("  [[FUTURE LOG]] Found and using classifier with dummy ID: " + localhostHash.getIndex());
        return model;
    }

    private int classify(double[] features, int knnParam, boolean eachVotes) {
        try {
            List<LocalRepresentativesModel> svmModels = new ArrayList<>();
            if (eachVotes) {
                svmModels = Arrays.asList(secondLocalModels);
            } else {
                int predictedByFirstLevelClassifierLabel = globalSvm.classify(features);
                LinearNNSearch knn = knnModelMap.get(predictedByFirstLevelClassifierLabel);
                if (knn == null) {
                    // should never be here, it may be cause of empty train set when random_percent = 0 for separated data
                    return predictedByFirstLevelClassifierLabel;
                }

                Instances instances = knn.kNearestNeighbours(WekaUtils.toInstance(features), knnParam);
                for (Instance i : instances) {
                    LocalRepresentativesModel secondLevelClassifier = secondLocalModels[(int) i.classValue()];
                    svmModels.add(secondLevelClassifier);
                    // TODO fixme group couintng
                }
            }

            return majorityVoting(features, svmModels);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int majorityVoting(double[] features, List<LocalRepresentativesModel> svmModels) {
        Map<Integer, Long> votes = svmModels.stream()
                .map(LocalRepresentativesModel::getSvmModel)
                .map(classificationModel -> classificationModel.classify(features))
                .collect(groupingBy(identity(), counting()));
        long maxVotes = votes.values()
                .stream()
                .max(naturalOrder())
                .get();
        return votes.entrySet()
                .stream()
                .filter(e -> e.getValue() == maxVotes)
                .findFirst()
                .get()
                .getKey();
    }

    @Override
    public String name() {
        return "SVN-2-LVL";
    }

}
