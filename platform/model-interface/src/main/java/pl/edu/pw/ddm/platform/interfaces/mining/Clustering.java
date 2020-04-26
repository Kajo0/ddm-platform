package pl.edu.pw.ddm.platform.interfaces.mining;

import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ResultCollector;
import pl.edu.pw.ddm.platform.interfaces.data.SampleProvider;

public interface Clustering extends MiningMethod {

    default String type() {
        return MethodType.CLUSTERING;
    }

    void cluster(SampleProvider sampleProvider, ParamProvider paramProvider, ResultCollector resultCollector);

}
