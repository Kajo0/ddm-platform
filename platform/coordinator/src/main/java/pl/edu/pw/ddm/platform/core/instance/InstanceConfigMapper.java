package pl.edu.pw.ddm.platform.core.instance;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

@Mapper
public interface InstanceConfigMapper {

    InstanceConfigMapper INSTANCE = Mappers.getMapper(InstanceConfigMapper.class);

    InstanceAddrDto map(InstanceConfig.InstanceNode node);

}
