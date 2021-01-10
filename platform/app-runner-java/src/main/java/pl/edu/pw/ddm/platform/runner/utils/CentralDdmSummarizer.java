package pl.edu.pw.ddm.platform.runner.utils;

import static pl.edu.pw.ddm.platform.runner.utils.ExecutionStatisticsPersister.Stats;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.time.DurationFormatUtils;
import pl.edu.pw.ddm.platform.runner.models.DatasetStatistics;
import pl.edu.pw.ddm.platform.runner.models.GlobalMethodModelWrapper;
import pl.edu.pw.ddm.platform.runner.models.ModelWrapper;
import pl.edu.pw.ddm.platform.runner.models.TimeStatistics;

@RequiredArgsConstructor
public class CentralDdmSummarizer {

    private final String masterAddr;
    private final List<String> workerAddrs;
    private final TimeStatistics globalStats;
    private final TimeStatistics trainingStats;
    private final TimeStatistics validationStats;

    private final List<List<ModelWrapper>> locals = new LinkedList<>();
    private final List<ModelWrapper> globals = new LinkedList<>();

    @Setter
    private List<ModelWrapper> executionAcks = new LinkedList<>();

    private Stats stats;

    public void addLocalModels(List<ModelWrapper> lms) {
        locals.add(lms);
    }

    public void addGlobalModel(ModelWrapper gm) {
        globals.add(gm);
    }

    public Stats prepareStats() {
        if (stats != null) {
            return stats;
        }

        ExecutionStatisticsPersister.TimeStats.TimeStatsBuilder time = ExecutionStatisticsPersister.TimeStats.builder();
        time.ddmTotalProcessing(globalStats.duration());
        time.ddmTotalTrainingProcessing(trainingStats.duration());
        time.ddmTotalValidationProcessing(validationStats.duration());
        globals.forEach(globalModel -> time.globalProcessing(globalModel.getTimeStatistics().duration()));
        locals.stream()
                .map(localModels -> localModels.stream()
                        .collect(Collectors.toMap(
                                ModelWrapper::getAddress,
                                lm -> ExecutionStatisticsPersister.TimeStats.LocalStats.of(
                                        lm.getTimeStatistics().duration(),
                                        lm.getTimeStatistics().getDataLoadingMillis())
                        )))
                .map(ExecutionStatisticsPersister.TimeStats.ProcessingWrapper::of)
                .forEach(time::localsProcessing);
        executionAcks.forEach(w -> time.localsExecution(
                w.getAddress(),
                ExecutionStatisticsPersister.TimeStats.LocalStats.of(
                        w.getTimeStatistics().duration(),
                        w.getTimeStatistics().getDataLoadingMillis()
                )));

        ExecutionStatisticsPersister.TransferStats.TransferStatsBuilder transfer = ExecutionStatisticsPersister.TransferStats.builder();
        globals.forEach(globalModel -> transfer.globalsByte(TransferSizeUtil.sizeOf(globalModel.getGlobalModel())));
        globals.stream()
                .filter(wrapper -> wrapper.getGlobalModel() instanceof GlobalMethodModelWrapper)
                .findFirst() // there is only one global method
                .ifPresent(wrapper -> transfer.globalMethodBytes(TransferSizeUtil.sizeOf(wrapper.getGlobalModel())));
        locals.stream()
                .map(localModels -> localModels.stream()
                        .collect(Collectors.toMap(
                                ModelWrapper::getAddress,
                                lm -> TransferSizeUtil.sizeOf(lm.getLocalModel())
                        ))).forEach(transfer::localsByte);

        ExecutionStatisticsPersister.DataStats.DataStatsBuilder data = ExecutionStatisticsPersister.DataStats.builder();
        locals.stream()
                .map(localModels -> localModels.stream()
                        .collect(Collectors.toMap(
                                ModelWrapper::getAddress,
                                lm -> lm.getDatasetStatistics().trainingDataSize())
                        )).forEach(data::trainingsByte);

        ExecutionStatisticsPersister.CustomMetrics.CustomMetricsBuilder metrics = ExecutionStatisticsPersister.CustomMetrics.builder();
        locals.stream()
                .map(localModels -> localModels.stream()
                        .collect(Collectors.toMap(
                                ModelWrapper::getAddress,
                                lm -> lm.getDatasetStatistics().getCustomMetrics())
                        )).forEach(metrics::local);
        globals.stream()
                .map(ModelWrapper::getDatasetStatistics)
                .map(DatasetStatistics::getCustomMetrics)
                .forEach(metrics::global);

        stats = Stats.of(time.build(), transfer.build(), data.build(), metrics.build());
        return stats;
    }

    public CentralDdmSummarizer printModelsSummary() {
        System.out.println("====== Models Summary:");
        System.out.println("  Local models:");
        for (int i = 0; i < locals.size(); ++i) {
            System.out.println("  [" + i + "] local stage");
            locals.get(i)
                    .forEach(System.out::println);
        }
        System.out.println("  Global models:");
        for (int i = 0; i < globals.size(); ++i) {
            System.out.println("  [" + i + "] global stage");
            System.out.println(globals.get(i));
        }
        System.out.println("  Execution acknowledges:");
        executionAcks.forEach(System.out::println);

        return this;
    }

    public CentralDdmSummarizer printDispersionSummary() {
        System.out.println("====== Dispersion Summary:");
        System.out.println("  Master address: " + masterAddr + " (" + (globals.isEmpty() ? "onlyLocal" : globals.get(0).getAddress()) + ")");
        System.out.println("  Available worker count: " + workerAddrs.size());
        System.out.println("    Used for local processing:");
        for (int i = 0; i < locals.size(); ++i) {
            System.out.println("  [" + i + "] local stage");
            nodeDispersionChecker(locals.get(i));
        }
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
        System.out.println("  Max building time (global): " + DurationFormatUtils.formatDurationHMS(time.getMaxGlobalProcessing()) + " (" + time.getMaxGlobalProcessing() + "ms)");
        System.out.println("  Total evaluation time: " + DurationFormatUtils.formatDurationHMS(time.getTotalExecution()) + " (" + time.getTotalExecution() + "ms)");
        System.out.println("  Total evaluation time loading: " + DurationFormatUtils.formatDurationHMS(time.getExecutionLoading()) + " (" + time.getExecutionLoading() + "ms)");
        System.out.println("  Total evaluation time without loading: " + DurationFormatUtils.formatDurationHMS(time.getTotalExecutionWithoutLoading()) + " (" + (time.getTotalExecutionWithoutLoading()) + "ms)");
        System.out.println("  Total max evaluation time without loading: " + DurationFormatUtils.formatDurationHMS(time.getMaxExecution()) + " (" + (time.getMaxExecution()) + "ms)");
        System.out.println("  Local processing (ms):");
        for (int i = 0; i < time.getLocalsProcessings().size(); ++i) {
            System.out.println("  [" + i + "] local stage");
            time.getLocalsProcessings()
                    .get(i)
                    .getMap()
                    .values()
                    .forEach(System.out::println);
        }
        System.out.println("  Global processing (ms):");
        System.out.println(time.getGlobalProcessing());
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
        for (int i = 0; i < transfer.getLocalsBytes().size(); ++i) {
            System.out.println("  [" + i + "] local stage");
            transfer.getLocalsBytes()
                    .get(i)
                    .values()
                    .forEach(System.out::println);
        }
        System.out.println("  Global model (bytes):");
        for (int i = 0; i < transfer.getGlobalsBytes().size(); ++i) {
            System.out.println("  [" + i + "] global stage");
            System.out.println(transfer.getGlobalsBytes().get(i));
        }

        return this;
    }

    public CentralDdmSummarizer printAvgDataSizeSummary() {
        ExecutionStatisticsPersister.DataStats data = prepareStats().getData();

        System.out.println("====== Training Data avg size Summary:");
        System.out.println("  Local models (bytes):");
        for (int i = 0; i < data.getTrainingsBytes().size(); ++i) {
            System.out.println("  [" + i + "] local stage");
            data.getTrainingsBytes()
                    .get(i)
                    .values()
                    .forEach(System.out::println);
        }

        return this;
    }

    public CentralDdmSummarizer printCustomMetricsSummary() {
        ExecutionStatisticsPersister.CustomMetrics metrics = prepareStats().getCustom();

        System.out.println("====== Custom Metrics Summary:");
        System.out.println("  Local models (metrics):");
        for (int i = 0; i < metrics.getLocals().size(); ++i) {
            System.out.println("  [" + i + "] local stage");
            metrics.getLocals()
                    .get(i)
                    .values()
                    .forEach(System.out::println);
        }
        System.out.println("  Global model (bytes):");
        for (int i = 0; i < metrics.getGlobals().size(); ++i) {
            System.out.println("  [" + i + "] global stage");
            System.out.println(metrics.getGlobals().get(i));
        }

        return this;
    }

    private void nodeDispersionChecker(List<ModelWrapper> models) {
        Map<String, Long> map = models.stream()
                .collect(Collectors.groupingBy(ModelWrapper::getAddress, Collectors.counting()));
        System.out.println("  Used workers (" + map.size() + "/" + workerAddrs.size() + "):");
        map.forEach((addr, count) -> System.out.println("[" + addr + "]: (" + count + "/1)"));
    }

}
