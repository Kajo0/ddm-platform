package pl.edu.pw.ddm.platform.runner.utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import pl.edu.pw.ddm.platform.runner.models.ModelWrapper;

public class CentralDdmSummarizer {

    private final List<ModelWrapper> localModels;
    private final ModelWrapper globalModel;
    private final List<ModelWrapper> updatedAcks;
    private final String masterAddr;
    private final List<String> workerAddrs;

    public CentralDdmSummarizer(List<ModelWrapper> localModels, ModelWrapper globalModel, List<ModelWrapper> updatedAcks, String masterAddr, List<String> workerAddrs) {
        this.localModels = localModels;
        this.globalModel = globalModel;
        this.updatedAcks = updatedAcks;
        this.masterAddr = masterAddr;
        this.workerAddrs = workerAddrs;
    }

    public CentralDdmSummarizer printModelsSummary() {
        System.out.println("====== Models Summary:");
        System.out.println("  Local models:");
        localModels.forEach(System.out::println);
        System.out.println("  Global model:");
        System.out.println(globalModel);
        System.out.println("  Updated acknowledges:");
        updatedAcks.forEach(System.out::println);

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

        return this;
    }

    private void nodeDispersionChecker(List<ModelWrapper> models) {
        Map<String, Long> map = models.stream()
                .collect(Collectors.groupingBy(ModelWrapper::getAddress, Collectors.counting()));
        System.out.println("  Used workers (" + map.size() + "/" + workerAddrs.size() + "):");
        map.forEach((addr, count) -> System.out.println("[" + addr + "]: (" + count + "/1)"));
    }

}
