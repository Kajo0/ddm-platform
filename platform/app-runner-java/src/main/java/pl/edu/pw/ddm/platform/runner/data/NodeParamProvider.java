package pl.edu.pw.ddm.platform.runner.data;

import lombok.AllArgsConstructor;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;

@AllArgsConstructor
public class NodeParamProvider implements ParamProvider {

    private final DistanceFunction distanceFunction;

    @Override
    public String provide(String name) {
        // TODO
        return null;
    }

    @Override
    public Double provideNumeric(String name) {
        // TODO
        return null;
    }

    @Override
    public DistanceFunction distanceFunction() {
        return distanceFunction;
    }

}
