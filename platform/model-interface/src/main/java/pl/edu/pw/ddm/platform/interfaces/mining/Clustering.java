package pl.edu.pw.ddm.platform.interfaces.mining;

import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ResultCollector;

public interface Clustering extends MiningMethod {

    void cluster(DataProvider dataProvider, ParamProvider paramProvider, ResultCollector resultCollector);

}
