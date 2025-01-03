package pl.edu.pw.ddm.platform.interfaces.algorithm;

import java.util.Collection;

import pl.edu.pw.ddm.platform.interfaces.algorithm.central.CentralDdmPipeline;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

/**
 * @deprecated Use {@link CentralDdmPipeline} instead
 */
@Deprecated
public interface GlobalProcessor<LModel extends LocalModel, GModel extends GlobalModel> {

    GModel processGlobal(Collection<LModel> localModels, ParamProvider paramProvider);

}
