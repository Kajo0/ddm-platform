package pl.edu.pw.ddm.platform.core.algorithm;

import java.util.List;

import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

interface AlgorithmBroadcaster {

    String broadcast(List<InstanceAddrDto> addresses, String algorithmId, byte[] jar);

}
