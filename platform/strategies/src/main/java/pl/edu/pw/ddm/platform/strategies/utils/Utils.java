package pl.edu.pw.ddm.platform.strategies.utils;

import com.google.common.collect.Iterables;
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Utils {

    private static final String BINDING_SIGN = "=";
    private static final String SEPARATION_SIGN = ";";

    public static Map<String, Double> simpleNumericParams(String params) {
        Map<String, Double> map = new HashMap<>();
        String[] separated = params.split(SEPARATION_SIGN);
        for (String str : separated) {
            String[] keyVal = str.split(BINDING_SIGN);
            if (keyVal.length != 2) {
                throw new IllegalStateException("Unsupported binding of param: " + str);
            }
            map.put(keyVal[0], Double.parseDouble(keyVal[1]));
        }
        return map;
    }

    // TODO optimize
    public static Map<String, Long> countLabels(PartitionerStrategy.DataDesc dataDesc) throws IOException {
        // TODO handle already partitioned
        var path = Path.of(Iterables.getOnlyElement(dataDesc.getFilesLocations()));

        return Files.readAllLines(path)
                .stream()
                .map(l -> l.split(dataDesc.getSeparator()))
                .collect(Collectors.groupingBy(d -> d[dataDesc.getLabelIndex()], Collectors.counting()));
    }

    // TODO optimize
    public static Map<Integer, String> mapIndexToLabel(PartitionerStrategy.DataDesc dataDesc) throws IOException {
        // TODO handle already partitioned
        var path = Path.of(Iterables.getOnlyElement(dataDesc.getFilesLocations()));

        return Files.readAllLines(path)
                .stream()
                .map(l -> l.split(dataDesc.getSeparator()))
                .collect(Collectors.toMap(d -> Integer.valueOf(d[dataDesc.getIdIndex()]),
                        d -> d[dataDesc.getLabelIndex()]));
    }

}
