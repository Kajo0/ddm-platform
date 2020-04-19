package pl.edu.pw.ddm.platform.core.results;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import pl.edu.pw.ddm.platform.core.results.dto.ValidationMetricsDto;
import pl.edu.pw.ddm.platform.metrics.dto.MetricsSummary;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MetricsResultsMapper {

    MetricsResultsMapper INSTANCE = Mappers.getMapper(MetricsResultsMapper.class);

    ValidationMetricsDto map(MetricsSummary summary);

}
