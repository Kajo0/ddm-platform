package pl.edu.pw.ddm.platform.testing.interfaces.impl;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import pl.edu.pw.ddm.platform.interfaces.algorithm.AlgorithmConfig;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;
import pl.edu.pw.ddm.platform.interfaces.mining.MiningMethod;

@Getter
@Builder
public final class DdmExecutionConfig {

    @NonNull
    private final AlgorithmConfig algorithmConfig;

    @NonNull
    private final MiningMethod miningMethod;

    @NonNull
    private final List<String> dataPath;

    private final String testDataPath;

    @NonNull
    private final String separator;

    @NonNull
    private final Integer idIndex;

    @NonNull
    private final Integer labelIndex;

    @NonNull
    private final Integer attributesAmount;

    private final String[] colTypes;
    private final DistanceFunction distanceFunction;

    @NonNull
    private final Map<String, String> executionParams;

}
