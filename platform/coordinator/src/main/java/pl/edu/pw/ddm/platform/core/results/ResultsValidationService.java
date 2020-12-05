package pl.edu.pw.ddm.platform.core.results;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.NonNull;
import pl.edu.pw.ddm.platform.metrics.dto.MetricsSummary;

interface ResultsValidationService {

    MetricsSummary validate(@NonNull String executionId, String dataId, String algorithmType, String[] metrics);

    @Data
    class ValidationMetrics {

        private Map<String, Double> metrics = new HashMap<>();
    }

}
