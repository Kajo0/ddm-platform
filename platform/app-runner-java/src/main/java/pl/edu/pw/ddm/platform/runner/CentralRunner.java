package pl.edu.pw.ddm.platform.runner;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.SneakyThrows;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;
import pl.edu.pw.ddm.platform.runner.models.ModelWrapper;
import pl.edu.pw.ddm.platform.runner.models.TimeStatistics;
import pl.edu.pw.ddm.platform.runner.utils.CentralDdmSummarizer;
import pl.edu.pw.ddm.platform.runner.utils.ExecutionStatisticsPersister;
import pl.edu.pw.ddm.platform.runner.utils.ExecutionStatusPersister;

public final class CentralRunner {

    private final JsonArgsDto args;
    private final InitParamsDto initParams;
    private final ExecutionStatusPersister statusPersister;

    private final JavaSparkContext sc;
    private final List<Integer> nodeStubList;

    private List<ModelWrapper> localModels;
    private ModelWrapper globalModel;
    private List<ModelWrapper> updatedAcks;
    private List<ModelWrapper> executionAcks;

    @SneakyThrows
    public CentralRunner(String jsonArgs) {
        this.args = JsonArgsDto.fromJson(jsonArgs);
        this.initParams = CentralRunner.createInitParams(args);

        this.nodeStubList = IntStream.range(0, args.getWorkerNodes().size())
                .boxed()
                .collect(Collectors.toList());
        this.localModels = new ArrayList<>(nodeStubList.size());
        printRunConfig();

        SparkContext ssc = SparkContext.getOrCreate();
        this.sc = JavaSparkContext.fromSparkContext(ssc);

        this.statusPersister = ExecutionStatusPersister.of(initParams.getExecutionId());
        this.statusPersister.init(this.sc.getConf().getAppId());
    }

    // TODO think if not remove or sth and log.info it
    private void printRunConfig() {
        System.out.println("------------------------------------------------------------------------");
        System.out.println("-             CentralRunner CONFIG                                     -");
        System.out.println("------------------------------------------------------------------------");
        System.out.println("  masterNode                  = " + args.getMasterNode());
        System.out.println("  workerNodes                 = " + args.getWorkerNodes());
        System.out.println("  algorithmId                 = " + args.getAlgorithmId());
        System.out.println("  algorithmPackageName        = " + args.getAlgorithmPackageName());
        System.out.println("  trainDataId                 = " + args.getTrainDataId());
        System.out.println("  testDataId                  = " + args.getTestDataId());
        System.out.println("  executionId                 = " + args.getExecutionId());
        System.out.println("  distanceFunctionName        = " + args.getDistanceFunctionName());
        System.out.println("  distanceFunctionPackageName = " + args.getDistanceFunctionPackageName());
        System.out.println("  executionParams             = " + args.getExecutionParams());
        System.out.println("  nodeStubList                = " + nodeStubList);
        System.out.println("------------------------------------------------------------------------");
    }

    public static void main(String[] args) {
        // TODO args with num dataId and maybe start parameters
        if (args.length != 1) {
            System.err.println("No json arg provided.");
            System.exit(1);
        }

        new CentralRunner(args[0])
                .run();
    }

    private void run() {
        statusPersister.started();
        // TODO send clear ID to every agent
        performEachNodeDistributionWorkaround();

        TimeStatistics stats = new TimeStatistics();
        stats.setStart(LocalDateTime.now());

        statusPersister.processLocal();
        processLocal();
        statusPersister.processGlobal();
        processGlobal();
        statusPersister.updateLocal();
        updateLocal();

        statusPersister.validate();
        executeMethod();
        stats.setEnd(LocalDateTime.now());

        statusPersister.summarize();
        CentralDdmSummarizer summarizer = new CentralDdmSummarizer(localModels, globalModel, updatedAcks, executionAcks, args.getMasterNode(), args.getWorkerNodes(), stats)
                .printModelsSummary()
                .printDispersionSummary()
                .printTimeSummary()
                .printTransferSummary();

        ExecutionStatisticsPersister.save(summarizer.prepareStats(), initParams.getExecutionId());

        statusPersister.finish();
        sc.stop();
    }

    private void performEachNodeDistributionWorkaround() {
        sc.parallelize(nodeStubList, nodeStubList.size())
                .map(n -> InetAddress.getLocalHost())
                .collect()
                .forEach(System.out::println);
    }

    private void processLocal() {
        // FIXME not using preferred locations
        localModels = sc.parallelize(nodeStubList, nodeStubList.size())
                .mapPartitions(new LocalProcessRunner(initParams))
                .collect();
    }

    @SneakyThrows
    private void processGlobal() {
        Iterator<LocalModel> models = localModels.stream()
                .map(ModelWrapper::getLocalModel)
                .iterator();
        globalModel = new GlobalProcessRunner(initParams).call(models)
                .next();
    }

    private void updateLocal() {
        List<GlobalModel> globals = Collections.nCopies(nodeStubList.size(), globalModel.getGlobalModel());
        updatedAcks = sc.parallelize(globals, nodeStubList.size())
                .mapPartitions(new LocalUpdateRunner(initParams))
                .collect();
    }

    private void executeMethod() {
        executionAcks = sc.parallelize(nodeStubList, nodeStubList.size())
                .mapPartitions(new LocalExecutionRunner(initParams))
                .collect();
    }

    private static InitParamsDto createInitParams(JsonArgsDto args) {
        return new InitParamsDto(
                args.getTrainDataId(),
                args.getTestDataId(),
                args.getExecutionId(),
                args.getAlgorithmPackageName(),
                args.getDistanceFunctionName(),
                args.getDistanceFunctionPackageName(),
                args.getExecutionParams()
        );
    }

}
