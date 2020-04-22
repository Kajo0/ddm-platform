package pl.edu.pw.ddm.platform.metrics.util;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ConversionUtils {

    public int[][] mapToInts(List<String> labels, List<String> predictions) {
        int[][] result = new int[2][labels.size()];

        AtomicInteger idVal = new AtomicInteger(0);
        HashMap<String, Integer> map = new HashMap<>();
        for (int i = 0; i < labels.size(); ++i) {
            String stringVal = labels.get(i);
            Integer intVal = map.get(stringVal);
            if (intVal == null) {
                map.put(stringVal, idVal.get());
                intVal = idVal.getAndIncrement();
            }
            result[0][i] = intVal;
        }
        for (int i = 0; i < predictions.size(); ++i) {
            String stringVal = predictions.get(i);
            Integer intVal = map.get(stringVal);
            if (intVal == null) {
                map.put(stringVal, idVal.get());
                intVal = idVal.getAndIncrement();
            }
            result[1][i] = intVal;
        }

        return result;
    }

}
