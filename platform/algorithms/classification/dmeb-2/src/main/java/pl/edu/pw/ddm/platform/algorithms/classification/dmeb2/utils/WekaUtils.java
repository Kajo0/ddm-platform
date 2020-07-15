package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class WekaUtils {

    static int classifyWeka(double[] features, Instances dataset, List<String> labels, Classifier model) {
        try {
            Instance instance = toInstance(features);
            instance.setDataset(dataset);
            return Integer.parseInt(labels.get((int) model.classifyInstance(instance)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static List<String> convertToLabels(List<LabeledObservation> observations) {
        return observations.stream()
                .map(LabeledObservation::getTarget)
                .sorted()
                .distinct()
                .map(target -> "" + target)
                .collect(toList());
    }

    public static Instances convertClusteringToInstances2(List<LabeledObservation> observations) {
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (int i = 0; i < observations.get(0).getFeatures().length; i++) {
            attributes.add(new Attribute("attr_" + i));
        }
        Instances instances = new Instances("dataset", attributes, 0);
        for (LabeledObservation o : observations) {
            instances.add(toInstance(o.getFeatures()));
        }
        return instances;
    }

    static Instances convertClusteringToInstances(List<UnlabeledObservation> observations) {
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (int i = 0; i < observations.get(0).getFeatures().length; i++) {
            attributes.add(new Attribute("attr_" + i));
        }
        Instances instances = new Instances("dataset", attributes, 0);
        for (UnlabeledObservation o : observations) {
            instances.add(toInstance(o.getFeatures()));
        }
        return instances;
    }

    public static Instances convertToInstances(List<LabeledObservation> observations, List<String> labels) {
        Map<Integer, Integer> labelMap = new HashMap<>();
        for (int i = 0; i < labels.size(); i++) {
            labelMap.put(Integer.parseInt(labels.get(i)), i);
        }

        ArrayList<Attribute> attributes = new ArrayList<>();
        for (int i = 0; i < observations.get(0).getFeatures().length; i++) {
            attributes.add(new Attribute("attr_" + i));
        }

        attributes.add(new Attribute("target", labels));

        Instances instances = new Instances("dataset", attributes, 0);
        for (LabeledObservation o : observations) {
            double[] values = new double[o.getFeatures().length + 1];
            System.arraycopy(o.getFeatures(), 0, values, 0, o.getFeatures().length);
            values[values.length - 1] = labelMap.get(o.getTarget());
            instances.add(toInstance(values));
        }
        instances.setClassIndex(instances.numAttributes() - 1);
        return instances;
    }

    public static Instance toInstance(double[] features) {
        return new DenseInstance(1.0, features);
    }
}
