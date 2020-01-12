package pl.edu.pw.ddm.platform.interfaces.algorithm;

import java.util.Collection;

import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

public interface GlobalProcessor {

    GlobalModel process(Collection<LocalModel> localModels, DataProvider dataProvider, ParamProvider paramProvider);

}
