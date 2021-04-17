package pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.CustomizableNormalize;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.Kernel;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.functions.supportVector.SMOset;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;
import weka.filters.unsupervised.attribute.Standardize;

@RequiredArgsConstructor
public class ExposingSVSMO extends SMO {

    private transient Set<LabeledObservation> svs;

    @Getter
    private final List<String> labels;

    private final transient double[] minAttrValues;
    private final transient double[] maxAttrValues;

    @Override
    public void buildClassifier(Instances insts) throws Exception {

        if (!m_checksTurnedOff) {
            // can classifier handle the data?
            getCapabilities().testWithFail(insts);

            // remove instances with missing class
            insts = new Instances(insts);
            insts.deleteWithMissingClass();

      /* Removes all the instances with weight equal to 0.
       MUST be done since condition (8) of Keerthi's paper
       is made with the assertion Ci > 0 (See equation (3a). */
            Instances data = new Instances(insts, insts.numInstances());
            for (int i = 0; i < insts.numInstances(); i++) {
                if (insts.instance(i).weight() > 0)
                    data.add(insts.instance(i));
            }
            if (data.numInstances() == 0) {
                throw new Exception("No training instances left after removing " +
                        "instances with weight 0!");
            }
            insts = data;
        }

        if (!m_checksTurnedOff) {
            m_Missing = new ReplaceMissingValues();
            m_Missing.setInputFormat(insts);
            insts = useFilter(insts, m_Missing);
        } else {
            m_Missing = null;
        }

        if (getCapabilities().handles(Capabilities.Capability.NUMERIC_ATTRIBUTES)) {
            boolean onlyNumeric = true;
            if (!m_checksTurnedOff) {
                for (int i = 0; i < insts.numAttributes(); i++) {
                    if (i != insts.classIndex()) {
                        if (!insts.attribute(i).isNumeric()) {
                            onlyNumeric = false;
                            break;
                        }
                    }
                }
            }

            if (!onlyNumeric) {
                m_NominalToBinary = new NominalToBinary();
                m_NominalToBinary.setInputFormat(insts);
                insts = useFilter(insts, m_NominalToBinary);
            } else {
                m_NominalToBinary = null;
            }
        } else {
            m_NominalToBinary = null;
        }

        if (m_filterType == FILTER_STANDARDIZE) {
            m_Filter = new Standardize();
            m_Filter.setInputFormat(insts);
            insts = useFilter(insts, m_Filter);
        } else if (m_filterType == FILTER_NORMALIZE) {
            if (minAttrValues != null && maxAttrValues != null) {
                m_Filter = new CustomizableNormalize(minAttrValues, maxAttrValues);
            } else {
                m_Filter = new Normalize();
            }
            m_Filter.setInputFormat(insts);
            insts = useFilter(insts, m_Filter);
        } else {
            m_Filter = null;
        }

        m_classIndex = insts.classIndex();
        m_classAttribute = insts.classAttribute();
        m_KernelIsLinear = (m_kernel instanceof PolyKernel) && (((PolyKernel) m_kernel).getExponent() == 1.0);

        // Generate subsets representing each class
        Instances[] subsets = new Instances[insts.numClasses()];
        for (int i = 0; i < insts.numClasses(); i++) {
            subsets[i] = new Instances(insts, insts.numInstances());
        }
        for (int j = 0; j < insts.numInstances(); j++) {
            Instance inst = insts.instance(j);
            subsets[(int) inst.classValue()].add(inst);
        }
        for (int i = 0; i < insts.numClasses(); i++) {
            subsets[i].compactify();
        }

        // Build the binary classifiers
        Random rand = new Random(m_randomSeed);
        m_classifiers = new BinarySMO[insts.numClasses()][insts.numClasses()];
        for (int i = 0; i < insts.numClasses(); i++) {
            for (int j = i + 1; j < insts.numClasses(); j++) {
                m_classifiers[i][j] = new BinarySMO();
                m_classifiers[i][j].setKernel(Kernel.makeCopy(getKernel()));
                Instances data = new Instances(insts, insts.numInstances());
                for (int k = 0; k < subsets[i].numInstances(); k++) {
                    data.add(subsets[i].instance(k));
                }
                for (int k = 0; k < subsets[j].numInstances(); k++) {
                    data.add(subsets[j].instance(k));
                }
                data.compactify();
                data.randomize(rand);
                invokeBuildClassifier(m_classifiers[i][j], data, i, j,
                        m_fitCalibratorModels,
                        m_numFolds, m_randomSeed);
            }
        }
    }

    private static Instances useFilter(Instances data, Filter filter) throws Exception {
        for (int i = 0; i < data.numInstances(); i++) {
            filter.input(data.instance(i));
        }
        filter.batchFinished();
        Instances newData = filter.getOutputFormat();
        Instance processed;
        int i = 0;
        while ((processed = filter.output()) != null) {
            newData.add(new InstanceWithPreviousVersion(processed, data.get(i)));
            i++;
        }
        return newData;
    }

    private static void invokeBuildClassifier(BinarySMO object, Instances insts, int cl1, int cl2, boolean fitCalibrator, int numFolds,
                                              int randomSeed) {
        try {
            Method method = object.getClass().getDeclaredMethod("buildClassifier", Instances.class, Integer.TYPE, Integer.TYPE, Boolean.TYPE, Integer.TYPE, Integer.TYPE);
            method.setAccessible(true);
            method.invoke(object, insts, cl1, cl2, fitCalibrator, numFolds, randomSeed);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Set<LabeledObservation> getSVs() {
        if (svs != null) {
             return svs;
        }
        svs = new HashSet<>();

        for (int i = 0; i < m_classifiers.length; i++) {
            for (int j = 0; j < m_classifiers[0].length; j++) {
                BinarySMO binarySMO = m_classifiers[i][j];
                if (binarySMO == null)
                    continue;
                SMOset m_supportVectors = (SMOset) getFieldValue(binarySMO, "m_supportVectors");
                Instances m_data = (Instances) getFieldValue(binarySMO, "m_data");

                int svCnt = m_supportVectors == null ? 0 : m_supportVectors.numElements();
                int index = m_data.numInstances() - 1;
                while (svCnt > 0 || index > 0) {
                    if (svCnt > 0 && m_supportVectors.contains(index)) {
                        Instance instance = m_data.get(index);
                        if (m_filterType != FILTER_NONE) {
                            instance = ((InstanceWithPreviousVersion) instance).getBefore();
                        }
                        double[] array = Arrays.copyOf(instance.toDoubleArray(), instance.toDoubleArray().length - 1);
                        int targetClass = (int) instance.value(instance.classIndex());
                        targetClass = Integer.parseInt(labels.get(targetClass)); // FIXME int label/index
                        svs.add(new LabeledObservation(-1, array, targetClass));
                        --svCnt;
                    } else {
                        m_data.set(index, NullInstance.INSTANCE);
                    }
                    --index;
                }
            }
        }
        return svs;
    }

    private static Object getFieldValue(BinarySMO binarySMO, String fieldName) {
        try {
            Field f = binarySMO.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(binarySMO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
