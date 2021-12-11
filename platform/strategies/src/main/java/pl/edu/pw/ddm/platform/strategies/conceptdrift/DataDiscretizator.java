package pl.edu.pw.ddm.platform.strategies.conceptdrift;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
class DataDiscretizator {

    private final Map<String, IdValuesPair> oneLabelData;
    private final int attrs;
    private final boolean[] numericAttrs;
    private final int discreteRanges;

    private final Map<String, IdValuesPair> finalMap = new HashMap<>();
    private final List<double[]> thresholds = new ArrayList<>();
    private double[] mins;
    private double[] maxes;

    Map<String, IdValuesPair> process() {
        if (!finalMap.isEmpty()) {
            return finalMap;
        }

        initialize();
        findMinMax();
        findThresholds();

        oneLabelData.forEach((id, pair) -> {
            var newPair = discretize(pair);
            finalMap.put(id, newPair);
        });

        return finalMap;
    }

    private IdValuesPair discretize(IdValuesPair pair) {
        String[] newValues = new String[pair.getValues().length];
        var values = pair.getValues();
        for (int i = 0; i < attrs; ++i) {
            if (numericAttrs[i]) {
                // TODO handle non numerical exceptions..
                double val = Double.parseDouble(values[i]);
                newValues[i] = discretize(val, i);
            } else {
                newValues[i] = values[i];
            }
        }
        return new IdValuesPair(pair.getId(), newValues);
    }

    private String discretize(double val, int attr) {
        var thresh = thresholds.get(attr);
        for (int i = 0; i < thresh.length - 1; ++i) {
            if (val >= thresh[i] && val < thresh[i + 1]) {
                return String.valueOf((thresh[i] + (thresh[i + 1] - thresh[i]) / 2));
            }
        }
        throw new IllegalStateException("Unknown range for value " + val + " from attr " + attr + " so wrong threshold setup");
    }

    private void initialize() {
        mins = new double[attrs];
        maxes = new double[attrs];

        for (int i = 0; i < attrs; ++i) {
            mins[i] = Double.MAX_VALUE;
            maxes[i] = -Double.MAX_VALUE;
        }
    }

    private void findMinMax() {
        oneLabelData.values()
                .stream()
                .map(IdValuesPair::getValues)
                .forEach(values -> {
                    for (int i = 0; i < attrs; ++i) {
                        if (numericAttrs[i]) {
                            // TODO handle non numerical exceptions..
                            double val = Double.parseDouble(values[i]);
                            if (mins[i] > val) {
                                mins[i] = val;
                            }
                            if (maxes[i] < val) {
                                maxes[i] = val;
                            }
                        }
                    }
                });
    }

    private void findThresholds() {
        // TODO more sophisticated thresholds according to value densities
        for (int i = 0; i < attrs; ++i) {
            var ranges = discreteRanges;
            if (discreteRanges == -1) {
                ranges = (int) Math.floor(Math.max(Math.log1p(maxes[i] - mins[i]) * 2, 4));
                log.info("Discretize ranges of attr {} into {} ranges.", i, ranges);
            }
            var thresh = findThresholds(mins[i], maxes[i], ranges);
            thresholds.add(thresh);
        }
    }

    private double[] findThresholds(double min, double max, int discreteRanges) {
        double[] result = new double[discreteRanges + 1];
        var step = (max - min) / discreteRanges;

        result[0] = min;
        for (int i = 1; i < discreteRanges; ++i) {
            result[i] = min + step * (i);
        }
        result[discreteRanges] = max + 1;

        return result;
    }

}
