package pl.edu.pw.ddm.platform.core.data;

import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

import java.util.List;

interface DataPartitioner {

    List<String> partitionData(DataLoader.DataDesc dataDesc,
                               String strategy,
                               String distanceFunction,
                               String params,
                               int nodes,
                               Long seed);

    String scatterTrain(List<InstanceAddrDto> addresses,
                        DataLoader.DataDesc dataDesc,
                        String strategy,
                        String distanceFunction,
                        String params,
                        Long seed);

    String scatterTestEqually(List<InstanceAddrDto> addresses,
                              DataLoader.DataDesc dataDesc,
                              String distanceFunction,
                              Long seed);

}
