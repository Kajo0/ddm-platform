package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils;

import static java.util.stream.Collectors.groupingBy;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    public List<MEBCluster> singleClassClusters() {
        return singleMap().getOrDefault(false, Collections.emptyList());
    }

    public List<MEBCluster> multiClassClusters() {
        return singleMap().getOrDefault(true, Collections.emptyList());
    }

    private Map<Boolean, List<MEBCluster>> singleMap() {
        return clusterList.stream()
                .collect(groupingBy(MEBCluster::isMultiClass));
    }

}
