package pl.edu.pw.ddm.platform.core.data;

import java.util.List;

import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

interface DataPartitioner {

    String scatter(List<InstanceAddrDto> addresses, LocalDataLoader.DataDesc data, String strategy);

}
