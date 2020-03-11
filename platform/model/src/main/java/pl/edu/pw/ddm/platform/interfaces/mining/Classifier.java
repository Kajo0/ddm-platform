package pl.edu.pw.ddm.platform.interfaces.mining;

import pl.edu.pw.ddm.platform.interfaces.data.ResultCollector;
import pl.edu.pw.ddm.platform.interfaces.data.SampleProvider;

public interface Classifier extends MiningMethod {

    void classify(SampleProvider sampleProvider, ResultCollector resultCollector);

}
