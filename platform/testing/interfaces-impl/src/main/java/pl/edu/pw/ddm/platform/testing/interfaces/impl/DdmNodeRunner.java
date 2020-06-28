package pl.edu.pw.ddm.platform.testing.interfaces.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.LocalProcessor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.LocalRepeater;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.LocalUpdater;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.MiningMethod;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class DdmNodeRunner {

    @Getter
    private final DataProvider dataProvider;

    private final ParamProvider paramProvider;

    @Setter
    private LocalProcessor localProcessor;

    @Setter
    private LocalRepeater localRepeater;

    @Setter
    private LocalUpdater localUpdater;

    private LocalModel localModel;

    @Getter
    private MiningMethod miningMethod;

    LocalModel processLocal() {
        localModel = localProcessor.processLocal(dataProvider, paramProvider);
        return localModel;
    }

    LocalModel repeatLocal(GlobalModel gModel) {
        localModel = localRepeater.repeatLocal(gModel, localModel, dataProvider, paramProvider);
        return localModel;
    }

    MiningMethod updateLocal(GlobalModel gModel) {
        miningMethod = localUpdater.updateLocal(localModel, gModel, dataProvider, paramProvider);
        return miningMethod;
    }

}
