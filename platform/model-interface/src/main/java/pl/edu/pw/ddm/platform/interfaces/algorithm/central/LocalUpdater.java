package pl.edu.pw.ddm.platform.interfaces.algorithm.central;

import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.MiningMethod;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

public interface LocalUpdater<LModel extends LocalModel, GModel extends GlobalModel, MMethod extends MiningMethod> extends Processor {

    MMethod updateLocal(LModel lModel, GModel gModel, DataProvider dataProvider, ParamProvider paramProvider);

}
