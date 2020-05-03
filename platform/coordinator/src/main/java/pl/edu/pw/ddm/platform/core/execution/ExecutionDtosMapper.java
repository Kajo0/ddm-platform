package pl.edu.pw.ddm.platform.core.execution;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import pl.edu.pw.ddm.platform.core.execution.dto.ExecutionDescDto;
import pl.edu.pw.ddm.platform.core.execution.dto.ExecutionStatsDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ExecutionDtosMapper {

    ExecutionDtosMapper INSTANCE = Mappers.getMapper(ExecutionDtosMapper.class);

    ExecutionDescDto map(ExecutionStarter.ExecutionDesc desc);

    ExecutionStatsDto map(ResultsCollector.ExecutionStats stats);

}
