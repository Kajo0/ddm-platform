package pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.SelectedTag;

public class WekaSVMClassification implements Serializable {

    private static final double LINEARLY_EXP = 1.000000000000001; // to keep support vectors

    private final Long seed;
    private final String kernelOptions;

    public WekaSVMClassification(String kernel, Long seed) {
        this.seed = seed;

        if (kernel == null) {
            throw new NullPointerException("kernel not provided");
        } else if ("rbf".equals(kernel)) {
            kernelOptions = "-C 12.5 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.RBFKernel -C -1 -G 0.50625\"";
        } else if ("linear".equals(kernel)) {
            kernelOptions = "-C 12.5 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -E " + LINEARLY_EXP + " -C -1\"";
        } else {
            throw new IllegalArgumentException("Unsupported kernel value: " + kernel);
        }
    }

    public SVMModel train(List<LabeledObservation> trainSet) {
        if (Utils.moreThanOneClass(trainSet)) {
            return doTrain(trainSet);
        } else {
            int target = trainSet.get(0)
                    .getTarget();
            return new DummySVMModel(target);
        }
    }

    private SVMModel doTrain(List<LabeledObservation> trainSet) {
        List<String> labels = WekaUtils.convertToLabels(trainSet);
        Instances dataset = WekaUtils.convertToInstances(trainSet, labels);
        ExposingSVSMO model = classifier(dataset);
        Instances headers = dataset.stringFreeStructure();
        return new SVMModel() {
            @Override
            public int classify(double[] features) {
                return WekaUtils.classifyWeka(features, headers, labels, model);
            }

            @Override
            public Set<LabeledObservation> getSVs() {
                return model.getSVs();
            }
        };
    }

    public ExposingSVSMO classifier(Instances dataset) {
        ExposingSVSMO model = new ExposingSVSMO();
        try {
            String[] options = weka.core.Utils.splitOptions(kernelOptions);
            model.setOptions(options);
            model.setChecksTurnedOff(true);
            model.setFilterType(new SelectedTag(SMO.FILTER_NORMALIZE, SMO.TAGS_FILTER));
            if (seed != null) {
                model.setRandomSeed(seed.intValue());
            }
            model.buildClassifier(dataset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return model;
    }
}
