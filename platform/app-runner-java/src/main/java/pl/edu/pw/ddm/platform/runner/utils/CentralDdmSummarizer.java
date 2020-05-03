package pl.edu.pw.ddm.platform.runner.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.time.DurationFormatUtils;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;
import pl.edu.pw.ddm.platform.runner.models.ModelWrapper;
import pl.edu.pw.ddm.platform.runner.models.TimeStatistics;

@AllArgsConstructor
public class CentralDdmSummarizer {

    private final List<ModelWrapper> localModels;
    private final ModelWrapper globalModel;
    private final List<ModelWrapper> updatedAcks;
    private final List<ModelWrapper> executionAcks;
    private final String masterAddr;
    private final List<String> workerAddrs;
    private final TimeStatistics globalStats;

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
        System.out.println("====== Time Summary:");
        System.out.println("  Total time (global): " + globalStats.durationPretty() + " (" + globalStats.duration() + "ms)");
        Long total = Stream.of(localModels, Collections.singletonList(globalModel), updatedAcks)
                .flatMap(Collection::stream)
                .map(ModelWrapper::getTimeStatistics)
                .map(TimeStatistics::duration)
                .reduce(0L, Long::sum);
        Long totalLoading = Stream.of(localModels, Collections.singletonList(globalModel), updatedAcks)
                .flatMap(Collection::stream)
                .map(ModelWrapper::getTimeStatistics)
                .map(TimeStatistics::getDataLoadingMillis)
                .reduce(0L, Long::sum);
        System.out.println("  Total time without data loading (global): " + DurationFormatUtils.formatDurationHMS(globalStats.duration() - totalLoading) + " (" + (globalStats.duration() - totalLoading) + "ms)");
        System.out.println("  Total time: " + DurationFormatUtils.formatDurationHMS(total) + " (" + total + "ms)");
        System.out.println("  Total time loading: " + DurationFormatUtils.formatDurationHMS(totalLoading) + " (" + totalLoading + "ms)");
        System.out.println("  Total time without loading: " + DurationFormatUtils.formatDurationHMS(total - totalLoading) + " (" + (total - totalLoading) + "ms)");
        System.out.println("  Local models (ms):");
        localModels.stream()
                .map(ModelWrapper::getTimeStatistics)
                .map(TimeStatistics::withoutDataLoading)
                .forEach(System.out::println);
        System.out.println("  Global model (ms):");
        System.out.println(globalModel.getTimeStatistics().duration());

        return this;
    }

    public CentralDdmSummarizer printTransferSummary() {
        System.out.println("====== Transfer Summary:");
        System.out.println("  Local models (bytes):");
        localModels.stream()
                .map(ModelWrapper::getLocalModel)
                .map(TransferSizeUtil::sizeOf)
                .forEach(System.out::println);
        System.out.println("  Global model (bytes):");
        System.out.println(TransferSizeUtil.sizeOf(globalModel.getGlobalModel()));

        return this;
    }

    private void nodeDispersionChecker(List<ModelWrapper> models) {
        Map<String, Long> map = models.stream()
                .collect(Collectors.groupingBy(ModelWrapper::getAddress, Collectors.counting()));
        System.out.println("  Used workers (" + map.size() + "/" + workerAddrs.size() + "):");
        map.forEach((addr, count) -> System.out.println("[" + addr + "]: (" + count + "/1)"));
    }

}
