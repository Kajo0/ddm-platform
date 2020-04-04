package pl.edu.pw.ddm.platform.core.execution;

import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

interface ExecutionStarter {

    String start(InstanceAddrDto masterAddr, String instanceId, String algorithmId, String dataId);

}
