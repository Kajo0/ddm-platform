package pl.edu.pw.ddm.platform.core.execution;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import pl.edu.pw.ddm.platform.core.execution.dto.ExecutionDescDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ExecutionStatusMapper {

    ExecutionStatusMapper INSTANCE = Mappers.getMapper(ExecutionStatusMapper.class);

    ExecutionDescDto map(ExecutionStarter.ExecutionDesc desc);

}
