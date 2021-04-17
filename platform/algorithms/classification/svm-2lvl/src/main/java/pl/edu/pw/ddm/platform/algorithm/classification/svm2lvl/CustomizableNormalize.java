package pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl;

import lombok.RequiredArgsConstructor;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.unsupervised.attribute.Normalize;

@RequiredArgsConstructor
public class CustomizableNormalize extends Normalize {

    private final double[] mins;
    private final double[] maxes;

    @Override
    public boolean batchFinished() throws Exception {
        if (getInputFormat() == null) {
            throw new IllegalStateException("No input instance format defined");
        }

        if (m_MinArray == null) {
            Instances input = getInputFormat();
            // Compute minimums and maximums
            m_MinArray = new double[input.numAttributes()];
            m_MaxArray = new double[input.numAttributes()];
            for (int i = 0; i < input.numAttributes(); i++) {
                m_MinArray[i] = Double.NaN;
            }

            for (int j = 0; j < input.numInstances(); j++) {
                double[] value = input.instance(j).toDoubleArray();
                for (int i = 0; i < input.numAttributes(); i++) {
                    if (input.attribute(i).isNumeric() && (input.classIndex() != i)) {
                        if (!Utils.isMissingValue(value[i])) {
                            if (Double.isNaN(m_MinArray[i])) {
                                m_MinArray[i] = m_MaxArray[i] = value[i];
                            } else {
                                if (value[i] < m_MinArray[i]) {
                                    m_MinArray[i] = value[i];
                                }
                                if (value[i] > m_MaxArray[i]) {
                                    m_MaxArray[i] = value[i];
                                }
                            }
                        }
                    }
                }
            }

            // Custom - non Weka code here
            for (int i = 0; i < mins.length; i++) {
                if (mins[i] < m_MinArray[i]) {
                    m_MinArray[i] = mins[i];
                }
                if (maxes[i] > m_MaxArray[i]) {
                    m_MaxArray[i] = maxes[i];
                }
            }
            // Custom - non Weka code here

            // Convert pending input instances
            for (int i = 0; i < input.numInstances(); i++) {
                convertInstance(input.instance(i));
            }
        }
        // Free memory
        flushInput();

        m_NewBatch = true;
        return (numPendingOutput() != 0);
    }

}
