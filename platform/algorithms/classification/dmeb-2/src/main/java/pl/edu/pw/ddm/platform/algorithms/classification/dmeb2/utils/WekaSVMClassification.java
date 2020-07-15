package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import weka.core.Instances;

public class WekaSVMClassification implements Serializable {

    private final String kernelOptions;

    public WekaSVMClassification(String kernel) {
        if (kernel == null) {
            throw new NullPointerException("kernel not provided");
        } else if ("rbf".equals(kernel)) {
            kernelOptions = "-C 12.5 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.RBFKernel -C 250007 -G 0.50625\"";
        } else if ("linear".equals(kernel)) {
            kernelOptions = "-C 12.5 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -E 1.0 -C 250007\"";
        } else {
            throw new IllegalArgumentException("Unsupported kernel value: " + kernel);
        }
    }

    public SVMModel train(List<LabeledObservation> trainSet) {
        if (Utils.moreThanOneClass(trainSet)) {
            return doTrain(trainSet);
        } else {
            return new SVMModel() {
                @Override
                public int classify(double[] features) {
                    // FIXME KJ
                    // think abou copy train set
//					 if (trainSet.isEmpty()) {
//						return 0;
//					}
                    return trainSet.get(0).getTarget();
                }

                @Override
                public List<LabeledObservation> getSVs() {
                    return Collections.emptyList();
                }
            };
        }
    }

    private SVMModel doTrain(List<LabeledObservation> trainSet) {
        List<String> labels = WekaUtils.convertToLabels(trainSet);
        Instances dataset = WekaUtils.convertToInstances(trainSet, labels);
        ExposingSVSMO model = classifier(dataset);
        return new SVMModel() {
            @Override
            public int classify(double[] features) {
                return WekaUtils.classifyWeka(features, dataset, labels, model);
            }

            @Override
            public List<LabeledObservation> getSVs() {
                return model.getSVs();
            }
        };
    }

    public ExposingSVSMO classifier(Instances dataset) {
        ExposingSVSMO model = new ExposingSVSMO();
        try {
            // FIXME KJ from parameter provider
            String[] options = weka.core.Utils.splitOptions(kernelOptions);
            model.setOptions(options);
            model.buildClassifier(dataset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return model;
    }
}
