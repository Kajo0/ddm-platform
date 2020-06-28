package pl.edu.pw.ddm.platform.interfaces.algorithm.central;

import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

public interface LocalProcessor<LModel extends LocalModel> extends Processor {

    LModel processLocal(DataProvider dataProvider, ParamProvider paramProvider);

}
