package pl.edu.pw.ddm.platform.core.data;

import java.util.List;

import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

interface DataPartitioner {

    String scatterTrain(List<InstanceAddrDto> addresses, DataLoader.DataDesc dataDesc, String strategy, String params);

    String scatterTestEqually(List<InstanceAddrDto> addresses, DataLoader.DataDesc dataDesc);

}
