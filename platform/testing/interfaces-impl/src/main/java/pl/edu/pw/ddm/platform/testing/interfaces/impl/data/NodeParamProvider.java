package pl.edu.pw.ddm.platform.testing.interfaces.impl.data;

import java.util.Map;
import java.util.Optional;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;

@RequiredArgsConstructor
public class NodeParamProvider implements ParamProvider {

    private final DistanceFunction distanceFunction;
    private final Map<String, String> executionParams;

    @Override
    public String provide(@NonNull String name) {
        return executionParams.get(name);
    }

    @Override
    public Double provideNumeric(@NonNull String name) {
        return Optional.of(name)
                .map(executionParams::get)
                .map(Double::valueOf)
                .orElse(null);
    }

    @Override
    public DistanceFunction distanceFunction() {
        return distanceFunction;
    }

}
