package pl.edu.pw.ddm.platform.interfaces.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface ParamProvider {

    String provide(String name);

    default String provide(String name, String defaultValue) {
        return Optional.ofNullable(name)
                .map(this::provide)
                .orElse(defaultValue);
    }

    Double provideNumeric(String name);

    default Double provideNumeric(String name, Double defaultValue) {
        return Optional.ofNullable(name)
                .map(this::provideNumeric)
                .orElse(defaultValue);
    }

    DistanceFunction distanceFunction();

    default Long seed() {
        Double seed = provideNumeric("seed");
        if (seed != null) {
            return seed.longValue();
        } else {
            return null;
        }
    }

    default Map<String, String> allParams() {
        Map<String, String> map = new HashMap<>(1);
        map.put("not", "implemented");
        return map;
    }

    default String prettyPrintParams() {
        StringBuilder str = new StringBuilder();
        str.append("---------------------------------").append("\n");
        str.append("-            PARAMS             -").append("\n");
        str.append("---------------------------------").append("\n");
        allParams().forEach((k, v) -> str.append("  ").append(k).append("=").append(v).append("\n"));
        str.append("---------------------------------");
        return str.toString();
    }

}
