package pl.edu.pw.ddm.platform.testing.interfaces.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.LocalProcessor;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.MiningMethod;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

/**
 * @deprecated Use {@link DdmNodeRunner}
 */
@Deprecated
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class NodeRunner {

    private final LocalProcessor localProcessor;

    @Getter
    private final DataProvider dataProvider;

    private final ParamProvider paramProvider;

    private LocalModel localModel;

    @Getter
    private MiningMethod miningMethod;

    LocalModel processLocal() {
        localModel = localProcessor.processLocal(dataProvider, paramProvider);
        return localModel;
    }

    MiningMethod updateLocal(GlobalModel globalModel) {
        miningMethod = localProcessor.updateLocal(localModel, globalModel, dataProvider, paramProvider);
        return miningMethod;
    }

}
