package pl.edu.pw.ddm.platform.interfaces.algorithm;

import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

public interface LocalProcessor {

    LocalModel process(DataProvider dataProvider, ParamProvider paramProvider);

}
