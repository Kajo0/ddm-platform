package pl.edu.pw.ddm.platform.interfaces.algorithm;

import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.MiningMethod;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

public interface LocalProcessor<LModel extends LocalModel, GModel extends GlobalModel, MMethod extends MiningMethod> {

    LModel processLocal(DataProvider dataProvider, ParamProvider paramProvider);

    MMethod updateLocal(LModel localModel, GModel globalModel, DataProvider dataProvider, ParamProvider paramProvider);

}
