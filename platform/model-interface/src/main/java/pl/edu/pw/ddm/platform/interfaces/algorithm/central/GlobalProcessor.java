package pl.edu.pw.ddm.platform.interfaces.algorithm.central;

import java.util.Collection;

import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

public interface GlobalProcessor<LModel extends LocalModel, GModel extends GlobalModel> extends Processor {

    GModel processGlobal(Collection<LModel> localModels, ParamProvider paramProvider);

}
