package pl.edu.pw.ddm.platform.core.data;

import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

interface DistanceFunctionBroadcaster {

    String broadcast(InstanceAddrDto masterAddr, DistanceFunctionLoader.DistanceFunctionDesc funcDesc);

}
