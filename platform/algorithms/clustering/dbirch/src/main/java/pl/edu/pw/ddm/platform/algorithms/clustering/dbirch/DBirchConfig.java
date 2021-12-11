package pl.edu.pw.ddm.platform.algorithms.clustering.dbirch;

import lombok.NoArgsConstructor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.AlgorithmConfig;
import pl.edu.pw.ddm.platform.interfaces.algorithm.DdmPipeline;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.CentralDdmPipeline;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class DBirchConfig implements AlgorithmConfig {

    @Override
    public DdmPipeline pipeline() {
        return CentralDdmPipeline.builder()
                .local(DBirch.class)
                .lastGlobal(DBirch.class);
    }

    @Override
    public boolean onlyNumerical() {
        return true;
    }

    @Override
    public boolean onlyNominal() {
        return false;
    }

    @Override
    public boolean onlySingleNode() {
        return false;
    }

    @Override
    public List<CustomParamDesc> availableParameters() {
        List<CustomParamDesc> params = new ArrayList<>();
        params.add(CustomParamDesc.of("branching_factor",
                Integer.class,
                "50",
                "Maximum number of CF sub-cluster in each node",
                "50 - when new sample occurs and 51. sub-cluster should be created, then split of the node is performed "));
        params.add(CustomParamDesc.of("threshold",
                Double.class,
                "0.5",
                "Minimum value of radius to start new sub-cluster",
                "0.1 - more splits",
                "10.0 - less splits, etc."));
        params.add(CustomParamDesc.of("groups",
                Integer.class,
                null,
                "Number of final groups (max leaves)",
                "3 - maximum 3 leaves allowed",
                "30 - maximum 30 leaves allowed"));
        params.add(CustomParamDesc.of("g_threshold",
                Double.class,
                "0.5",
                "Minimum value of radius to start new sub-cluster for global step",
                "0.1 - more splits",
                "10.0 - less splits, etc."));
        params.add(CustomParamDesc.of("g_groups",
                Integer.class,
                null,
                "Number of final groups (max leaves) for global step",
                "3 - maximum 3 leaves allowed",
                "30 - maximum 30 leaves allowed"));
        return params;
    }

}