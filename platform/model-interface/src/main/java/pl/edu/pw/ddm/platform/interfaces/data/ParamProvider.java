package pl.edu.pw.ddm.platform.interfaces.data;

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

}
