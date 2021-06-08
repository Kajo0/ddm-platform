package pl.edu.pw.ddm.platform.core.data;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

// TODO optimize by using one pass: https://stackoverflow.com/a/47234669
class DataStatisticsCalculator {

    private final Map<String, Integer> classCountMap = new HashMap<>();
    private final Map<String, double[]> classSumMap = new HashMap<>();
    private final Map<String, double[]> classStdDevSumMap = new HashMap<>();

    @Getter
    private final Map<String, double[]> classStdDevMap = new HashMap<>();

    @Getter
    private final Map<String, double[]> classMeanMap = new HashMap<>();

    public void countSum(String label, double[] attrs) {
        var count = classCountMap.getOrDefault(label, 0);
        classCountMap.put(label, ++count);

        var sum = classSumMap.get(label);
        if (sum == null) {
            sum = attrs;
        } else {
            for (int i = 0; i < attrs.length; ++i) {
                sum[i] += attrs[i];
            }
        }
        classSumMap.put(label, sum);
    }

    public void mean() {
        classSumMap.entrySet()
                .stream()
                .forEach(entry -> {
                    var label = entry.getKey();
                    var sum = entry.getValue();

                    var count = classCountMap.get(label);
                    var means = new double[sum.length];
                    for (int i = 0; i < means.length; ++i) {
                        means[i] = sum[i] / count;
                    }

                    classMeanMap.put(label, means);
                });
    }

    public void stddevSum(String label, double[] attrs) {
        var stddev = classStdDevSumMap.get(label);
        if (stddev == null) {
            stddev = new double[attrs.length];
        }
        var mean = classMeanMap.get(label);
        for (int i = 0; i < attrs.length; ++i) {
            stddev[i] += Math.pow(attrs[i] - mean[i], 2);
        }
        classStdDevSumMap.put(label, stddev);
    }

    public void stddev() {
        classStdDevSumMap.entrySet()
                .stream()
                .forEach(entry -> {
                    var label = entry.getKey();
                    var stddevSum = entry.getValue();

                    var count = classCountMap.get(label);

                    var stddevs = new double[stddevSum.length];
                    for (int i = 0; i < stddevs.length; ++i) {
                        stddevs[i] = Math.sqrt(stddevSum[i] / count);
                    }

                    classStdDevMap.put(label, stddevs);
                });
    }

}
