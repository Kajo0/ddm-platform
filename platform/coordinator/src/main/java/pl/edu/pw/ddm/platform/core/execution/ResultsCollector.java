package pl.edu.pw.ddm.platform.core.execution;

import java.util.List;

import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

interface ResultsCollector {

    String collect(List<InstanceAddrDto> addresses, ExecutionStarter.ExecutionDesc desc);

}
