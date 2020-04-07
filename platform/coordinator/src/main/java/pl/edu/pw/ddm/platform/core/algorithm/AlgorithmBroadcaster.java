package pl.edu.pw.ddm.platform.core.algorithm;

import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

interface AlgorithmBroadcaster {

    String broadcast(InstanceAddrDto masterAddr, AlgorithmLoader.AlgorithmDesc algorithmDesc);

}
