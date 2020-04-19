package pl.edu.pw.ddm.platform.metrics.util;

import java.util.List;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ConversionUtils {

    public int[] mapToInts(List<String> values) {
        return values.stream()
                .mapToInt(String::hashCode)
                .toArray();
    }

}
