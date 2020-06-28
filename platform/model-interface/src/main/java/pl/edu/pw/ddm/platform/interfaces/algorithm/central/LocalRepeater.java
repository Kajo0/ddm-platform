package pl.edu.pw.ddm.platform.interfaces.algorithm.central;

import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

public interface LocalRepeater<LPrevModel extends LocalModel, LUpdModel extends LocalModel, GModel extends GlobalModel> extends Processor {

    LUpdModel repeatLocal(GModel gModel, LPrevModel lModel, DataProvider dataProvider, ParamProvider paramProvider);

}
