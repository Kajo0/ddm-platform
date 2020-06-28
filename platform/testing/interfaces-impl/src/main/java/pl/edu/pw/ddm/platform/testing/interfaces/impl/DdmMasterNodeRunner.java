package pl.edu.pw.ddm.platform.testing.interfaces.impl;

import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.GlobalProcessor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.GlobalUpdater;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.MiningMethod;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class DdmMasterNodeRunner {

    private final ParamProvider paramProvider;

    @Setter
    private GlobalProcessor globalProcessor;

    @Setter
    private GlobalUpdater globalUpdater;

    @Getter
    private MiningMethod miningMethod;

    GlobalModel processGlobal(List<LocalModel> localModels) {
        return globalProcessor.processGlobal(localModels, paramProvider);
    }

    MiningMethod updateGlobal(List<LocalModel> localModels) {
        miningMethod = globalUpdater.updateGlobal(localModels, paramProvider);
        return miningMethod;
    }

}
