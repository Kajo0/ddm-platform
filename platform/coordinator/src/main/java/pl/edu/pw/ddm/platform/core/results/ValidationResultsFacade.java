package pl.edu.pw.ddm.platform.core.results;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.pw.ddm.platform.core.algorithm.AlgorithmFacade;
import pl.edu.pw.ddm.platform.core.execution.ExecutionFacade;
import pl.edu.pw.ddm.platform.core.results.dto.ValidationMetricsDto;
import pl.edu.pw.ddm.platform.interfaces.mining.MiningMethod;
import pl.edu.pw.ddm.platform.metrics.dto.MetricsSummary;

@Slf4j
@Service
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ValidationResultsFacade {

    private final ExecutionFacade executionFacade;
    private final AlgorithmFacade algorithmFacade;
    private final ResultsValidationService validationService;

    public ValidationMetricsDto validate(@NonNull ValidateRequest request) {
        var executionReq = ExecutionFacade.StatusRequest.of(request.executionId);
        var executionStatus = executionFacade.status(executionReq);

        var algorithmReq = AlgorithmFacade.DescriptionRequest.of(executionStatus.getAlgorithmId());
        var algorithmDesc = algorithmFacade.description(algorithmReq);

        // TODO check if results collected and no error found
        // TODO check if data exists

        String dataId;
        switch (algorithmDesc.getAlgorithmType()) {
            case MiningMethod.MethodType.CLUSTERING:
                dataId = executionStatus.getTrainDataId();
                break;
            case MiningMethod.MethodType.CLASSIFICATION:
                dataId = executionStatus.getTestDataId();
                break;

            default:
                throw new IllegalArgumentException("Unknown algorithm type: " + algorithmDesc.getAlgorithmType());
        }

        MetricsSummary summary = validationService.validate(request.executionId, dataId, algorithmDesc.getAlgorithmType(), request.metrics);
        return MetricsResultsMapper.INSTANCE.map(summary);
    }

    @Builder
    public static class ValidateRequest {

        @NonNull
        private final String executionId;

        @NonNull
        private final String[] metrics;
    }

}
