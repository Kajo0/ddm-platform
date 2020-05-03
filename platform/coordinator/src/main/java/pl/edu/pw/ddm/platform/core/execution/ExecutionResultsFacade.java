package pl.edu.pw.ddm.platform.core.execution;

import java.io.File;
import java.util.List;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import org.springframework.stereotype.Service;
import pl.edu.pw.ddm.platform.core.execution.dto.ExecutionStatsDto;
import pl.edu.pw.ddm.platform.core.instance.InstanceFacade;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;

@Service
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ExecutionResultsFacade {

    private final InstanceFacade instanceFacade;
    private final ExecutionStarter executionStarter;
    private final ResultsCollector resultsCollector;

    public String collectResults(@NonNull CollectResultsRequest request) {
        ExecutionStarter.ExecutionDesc desc = executionStarter.status(request.executionId);
        var req = InstanceFacade.AddressRequest.of(desc.getInstanceId());
        List<InstanceAddrDto> addresses = instanceFacade.addresses(req);

        return resultsCollector.collect(addresses, desc);
    }

    public File[] nodesResultsFiles(@NonNull LoadResultFilesRequest request) {
        return resultsCollector.load(request.executionId);
    }

    public ExecutionStatsDto stats(@NonNull StatsRequest request) {
        return Optional.of(request.executionId)
                .map(resultsCollector::loadStats)
                .map(ExecutionDtosMapper.INSTANCE::map)
                .orElse(null);
    }

    @Value(staticConstructor = "of")
    public static class CollectResultsRequest {

        @NonNull
        private final String executionId;
    }

    @Value(staticConstructor = "of")
    public static class LoadResultFilesRequest {

        @NonNull
        private final String executionId;
    }

    @Value(staticConstructor = "of")
    public static class StatsRequest {

        @NonNull
        private final String executionId;
    }

}
