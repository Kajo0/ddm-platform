package pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils;

import java.io.Serializable;
import java.util.List;

public class MEBModel implements Serializable {

    private final List<MEBCluster> clusterList;
    private final Integer partitionId;

    public MEBModel(List<MEBCluster> clusterList, Integer partitionId) {
        this.partitionId = partitionId;
        this.clusterList = clusterList;
    }

    public List<MEBCluster> getClusterList() {
        return clusterList;
    }

    public Integer getPartitionId() {
        return partitionId;
    }
}
