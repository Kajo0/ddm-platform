package pl.edu.pw.ddm.platform.core.coordinator;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import pl.edu.pw.ddm.platform.core.instance.InstanceFacade;

@Mapper
public interface InstanceDataMapper {

    InstanceDataMapper INSTANCE = Mappers.getMapper(InstanceDataMapper.class);

    InstanceFacade.SetupRequest.ManualSetupNode map(InstanceManualSetupCommandController.ManualSetupNode node);

}
