package pl.edu.pw.ddm.platform.core.execution;

import java.io.File;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import org.springframework.stereotype.Service;
import pl.edu.pw.ddm.platform.core.execution.dto.ExecutionStatsDto;
import pl.edu.pw.ddm.platform.core.instance.InstanceFacade;

@Service
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ExecutionResultsFacade {

    private final InstanceFacade instanceFacade;
    private final ExecutionStarter executionStarter;
    private final ResultsCollector resultsCollector;
    private final LogsCollector logsCollector;

    public String collectResults(@NonNull CollectResultsRequest request) {
        var desc = executionStarter.status(request.executionId);
        var req = InstanceFacade.AddressRequest.of(desc.getInstanceId());
        var addresses = instanceFacade.addresses(req);

        return resultsCollector.collect(addresses, desc);
    }

    public String collectLogs(@NonNull CollectLogsRequest request) {
        var desc = executionStarter.status(request.executionId);
        var req = InstanceFacade.AddressRequest.of(desc.getInstanceId());
        var addresses = instanceFacade.addresses(req);

        return logsCollector.collectAll(addresses, desc);
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
    public static class CollectLogsRequest {

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
