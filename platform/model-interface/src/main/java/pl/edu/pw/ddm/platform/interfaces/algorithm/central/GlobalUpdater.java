package pl.edu.pw.ddm.platform.interfaces.algorithm.central;

import java.util.Collection;

import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.MiningMethod;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

public interface GlobalUpdater<LModel extends LocalModel, MMethod extends MiningMethod> extends Processor {

    MMethod updateGlobal(Collection<LModel> localModels, ParamProvider paramProvider);

}
