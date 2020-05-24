package pl.edu.pw.ddm.platform.runner.utils;

import static pl.edu.pw.ddm.platform.runner.utils.ExecutionStatisticsPersister.Stats;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DurationFormatUtils;
import pl.edu.pw.ddm.platform.runner.models.ModelWrapper;
import pl.edu.pw.ddm.platform.runner.models.TimeStatistics;

@RequiredArgsConstructor
public class CentralDdmSummarizer {

    private final List<ModelWrapper> localModels;
    private final ModelWrapper globalModel;
    private final List<ModelWrapper> updatedAcks;
    private final List<ModelWrapper> executionAcks;
    private final String masterAddr;
    private final List<String> workerAddrs;
    private final TimeStatistics globalStats;

    private Stats stats;

    public Stats prepareStats() {
        if (stats != null) {
            return stats;
        }

        ExecutionStatisticsPersister.TimeStats.TimeStatsBuilder time = ExecutionStatisticsPersister.TimeStats.builder();
        time.ddmTotalProcessing(globalStats.duration());
        time.globalProcessing(globalModel.getTimeStatistics().duration());
        localModels.forEach(w -> time.localsProcessing(w.getAddress(), ExecutionStatisticsPersister.TimeStats.LocalStats.of(w.getTimeStatistics().duration(), w.getTimeStatistics().getDataLoadingMillis())));
        updatedAcks.forEach(w -> time.localsUpdate(w.getAddress(), ExecutionStatisticsPersister.TimeStats.LocalStats.of(w.getTimeStatistics().duration(), w.getTimeStatistics().getDataLoadingMillis())));
        executionAcks.forEach(w -> time.localsExecution(w.getAddress(), ExecutionStatisticsPersister.TimeStats.LocalStats.of(w.getTimeStatistics().duration(), w.getTimeStatistics().getDataLoadingMillis())));

        ExecutionStatisticsPersister.TransferStats.TransferStatsBuilder transfer = ExecutionStatisticsPersister.TransferStats.builder();
        transfer.globalBytes(TransferSizeUtil.sizeOf(globalModel.getGlobalModel()));
        localModels.forEach(w -> transfer.localsByte(w.getAddress(), TransferSizeUtil.sizeOf(w.getLocalModel())));

        stats = Stats.of(time.build(), transfer.build());
        return stats;
    }

    public CentralDdmSummarizer printModelsSummary() {
        System.out.println("====== Models Summary:");
        System.out.println("  Local models:");
        localModels.forEach(System.out::println);
        System.out.println("  Global model:");
        System.out.println(globalModel);
        System.out.println("  Updated acknowledges:");
        updatedAcks.forEach(System.out::println);
        System.out.println("  Execution acknowledges:");
        executionAcks.forEach(System.out::println);

        return this;
    }

    public CentralDdmSummarizer printDispersionSummary() {
        System.out.println("====== Dispersion Summary:");
        System.out.println("  Master address: " + masterAddr + " (" + globalModel.getAddress() + ")");
        System.out.println("  Available worker count: " + workerAddrs.size());
        System.out.println("    Used for local processing:");
        nodeDispersionChecker(localModels);
        System.out.println("    Used for local update:");
        nodeDispersionChecker(updatedAcks);
        System.out.println("    Used for execution update:");
        nodeDispersionChecker(executionAcks);

        return this;
    }

    public CentralDdmSummarizer printTimeSummary() {
        ExecutionStatisticsPersister.TimeStats time = prepareStats().getTime();

        System.out.println("====== Time Summary:");
        System.out.println("  Total time (ddm global): " + DurationFormatUtils.formatDurationHMS(time.getDdmTotalProcessing()) + " (" + time.getDdmTotalProcessing() + "ms)");
        System.out.println("  Total time without max data loading (ddm global): " + DurationFormatUtils.formatDurationHMS(time.getDdmTotalWithoutMaxLoadings()) + " (" + (time.getDdmTotalWithoutMaxLoadings()) + "ms)");
        System.out.println("  Total building time (local + global): " + DurationFormatUtils.formatDurationHMS(time.getTotal()) + " (" + time.getTotal() + "ms)");
        System.out.println("  Total building time loading (local + global): " + DurationFormatUtils.formatDurationHMS(time.getLocalLoading()) + " (" + time.getLocalLoading() + "ms)");
        System.out.println("  Total building time without loading (local + global): " + DurationFormatUtils.formatDurationHMS(time.getTotalWithoutLoading()) + " (" + (time.getTotalWithoutLoading()) + "ms)");
        System.out.println("  Total max building time without loading (max local + global): " + DurationFormatUtils.formatDurationHMS(time.getTotalMaxProcessing()) + " (" + (time.getTotalMaxProcessing()) + "ms)");
        System.out.println("  Total building time (global): " + DurationFormatUtils.formatDurationHMS(time.getGlobalProcessing()) + " (" + time.getGlobalProcessing() + "ms)");
        System.out.println("  Total evaluation time: " + DurationFormatUtils.formatDurationHMS(time.getTotalExecution()) + " (" + time.getTotalExecution() + "ms)");
        System.out.println("  Total evaluation time loading: " + DurationFormatUtils.formatDurationHMS(time.getExecutionLoading()) + " (" + time.getExecutionLoading() + "ms)");
        System.out.println("  Total evaluation time without loading: " + DurationFormatUtils.formatDurationHMS(time.getTotalExecutionWithoutLoading()) + " (" + (time.getTotalExecutionWithoutLoading()) + "ms)");
        System.out.println("  Total max evaluation time without loading: " + DurationFormatUtils.formatDurationHMS(time.getMaxExecution()) + " (" + (time.getMaxExecution()) + "ms)");
        System.out.println("  Local processing (ms):");
        time.getLocalsProcessings()
                .values()
                .forEach(System.out::println);
        System.out.println("  Global processing (ms):");
        System.out.println(time.getGlobalProcessing());
        System.out.println("  Local update (ms):");
        time.getLocalsUpdates()
                .values()
                .forEach(System.out::println);
        System.out.println("  Local execution (ms):");
        time.getLocalsExecutions()
                .values()
                .forEach(System.out::println);

        return this;
    }

    public CentralDdmSummarizer printTransferSummary() {
        ExecutionStatisticsPersister.TransferStats transfer = prepareStats().getTransfer();

        System.out.println("====== Transfer Summary:");
        System.out.println("  Local models (bytes):");
        transfer.getLocalsBytes()
                .values()
                .forEach(System.out::println);
        System.out.println("  Global model (bytes):");
        System.out.println(transfer.getGlobalBytes());

        return this;
    }

    private void nodeDispersionChecker(List<ModelWrapper> models) {
        Map<String, Long> map = models.stream()
                .collect(Collectors.groupingBy(ModelWrapper::getAddress, Collectors.counting()));
        System.out.println("  Used workers (" + map.size() + "/" + workerAddrs.size() + "):");
        map.forEach((addr, count) -> System.out.println("[" + addr + "]: (" + count + "/1)"));
    }

}
