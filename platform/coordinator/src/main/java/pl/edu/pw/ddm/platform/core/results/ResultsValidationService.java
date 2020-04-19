package pl.edu.pw.ddm.platform.core.results;

import lombok.NonNull;
import pl.edu.pw.ddm.platform.metrics.dto.MetricsSummary;

interface ResultsValidationService {

    MetricsSummary validate(@NonNull String executionId, String dataId, String algorithmType, String[] metrics);

}
