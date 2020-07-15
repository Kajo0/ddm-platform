package pl.edu.pw.ddm.platform.runner;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import pl.edu.pw.ddm.platform.interfaces.algorithm.DdmPipeline;
import pl.edu.pw.ddm.platform.interfaces.mining.MiningMethod;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;
import pl.edu.pw.ddm.platform.runner.models.GlobalMethodModelWrapper;
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
    private CentralDdmSummarizer summarizer;

    // FIXME sneaky throws log to std out
    @SneakyThrows
    public CentralRunner(String jsonArgs) {
        this.args = JsonArgsDto.fromJson(jsonArgs);
        this.initParams = CentralRunner.createInitParams(args);

        this.nodeStubList = IntStream.range(0, args.getWorkerNodes().size())
                .boxed()
                .collect(Collectors.toList());
        printRunConfig();

        SparkContext ssc = SparkContext.getOrCreate();
        this.sc = JavaSparkContext.fromSparkContext(ssc);

        this.statusPersister = ExecutionStatusPersister.of(initParams.getExecutionPath(), initParams.getExecutionId());
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
        System.out.println("  pipeline                    = " + args.getPipeline());
        System.out.println("  executionPath               = " + args.getExecutionPath());
        System.out.println("  datasetsPath                = " + args.getDatasetsPath());
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
        // TODO Improve catching exception per node or sth similar
        try {
            runInternal();
        } catch (Exception e) {
            // TODO get failed node address from exception message or from sc if possible
            System.err.println("Exception occurred: " + e.getMessage());
            e.printStackTrace();
            statusPersister.error(e.getMessage());
        } finally {
            sc.stop();
        }
    }

    private void runInternal() {
        // TODO send clear ID to every agent
        performEachNodeDistributionWorkaround();
        statusPersister.started();

        TimeStatistics stats = new TimeStatistics();
        stats.setStart(LocalDateTime.now());
        summarizer = new CentralDdmSummarizer(args.getMasterNode(), args.getWorkerNodes(), stats);

        args.pipeline()
                .getStages()
                .forEach(this::processStage);

        statusPersister.validate();
        List<ModelWrapper> acks = executeMethod();
        summarizer.setExecutionAcks(acks);
        stats.setEnd(LocalDateTime.now());

        statusPersister.summarize();
        ExecutionStatisticsPersister.save(initParams.getExecutionPath(), summarizer.prepareStats(), initParams.getExecutionId());
        summarizer.printModelsSummary()
                .printDispersionSummary()
                .printTimeSummary()
                .printTransferSummary();

        statusPersister.finish();
    }

    private void processStage(DdmPipeline.ProcessingStage processingStage) {
        DdmPipeline.Stage stage = processingStage.getStage();

        switch (stage) {
            case LOCAL: {
                statusPersister.processLocal();
                localModels = processLocal(processingStage);
                summarizer.addLocalModels(localModels);
                return;
            }
            case GLOBAL: {
                statusPersister.processGlobal();
                globalModel = processGlobal(processingStage);
                summarizer.addGlobalModel(globalModel);
                return;
            }
            case LOCAL_REPEAT: {
                statusPersister.repeatLocal();
                localModels = repeatLocal(processingStage);
                summarizer.addLocalModels(localModels);
                return;
            }
            case GLOBAL_UPDATE: {
                statusPersister.updateGlobal();
                // global model is method wrapped into model
                globalModel = updateGlobal(processingStage);
                summarizer.addGlobalModel(globalModel);
                return;
            }
            case LOCAL_UPDATE: {
                statusPersister.updateLocal();
                localModels = updateLocal(processingStage);
                return;
            }
            default:
                throw new UnsupportedOperationException("Unknown DDM pipeline processing stage");
        }
    }

    private void performEachNodeDistributionWorkaround() {
        sc.parallelize(nodeStubList, nodeStubList.size())
                .map(n -> InetAddress.getLocalHost())
                .collect()
                .forEach(System.out::println);
    }

    @SneakyThrows
    private List<ModelWrapper> processLocal(DdmPipeline.ProcessingStage processingStage) {
        // FIXME not using preferred locations
        List<ModelWrapper> localModels = sc.parallelize(nodeStubList, nodeStubList.size())
                .mapPartitions(new LocalProcessRunner(initParams, processingStage.processor(), processingStage.getStageIndex()))
                .collect();
        verifyUniqueNodeExecutors(localModels);
        return localModels;
    }

    @SneakyThrows
    private ModelWrapper processGlobal(DdmPipeline.ProcessingStage processingStage) {
        Iterator<LocalModel> models = localModels.stream()
                .map(ModelWrapper::getLocalModel)
                .iterator();
        return new GlobalProcessRunner(initParams, processingStage.processor(), processingStage.getStageIndex())
                .call(models)
                .next();
    }

    @SneakyThrows
    private List<ModelWrapper> repeatLocal(DdmPipeline.ProcessingStage processingStage) {
        List<GlobalModel> globals = Collections.nCopies(nodeStubList.size(), globalModel.getGlobalModel());
        List<ModelWrapper> localModels = sc.parallelize(globals, nodeStubList.size())
                .mapPartitions(new LocalRepeatRunner(initParams, processingStage.processor(), processingStage.getStageIndex()))
                .collect();
        verifyUniqueNodeExecutors(localModels);
        return localModels;
    }

    @SneakyThrows
    private List<ModelWrapper> updateLocal(DdmPipeline.ProcessingStage processingStage) {
        GlobalModel gmodel = Optional.ofNullable(globalModel)
                .map(ModelWrapper::getGlobalModel)
                .orElse(null);
        List<GlobalModel> globals = Collections.nCopies(nodeStubList.size(), gmodel);
        List<ModelWrapper> localModels = sc.parallelize(globals, nodeStubList.size())
                .mapPartitions(new LocalUpdateRunner(initParams, processingStage.processor(), processingStage.getStageIndex()))
                .collect();
        verifyUniqueNodeExecutors(localModels);
        return localModels;
    }

    @SneakyThrows
    private ModelWrapper updateGlobal(DdmPipeline.ProcessingStage processingStage) {
        Iterator<LocalModel> models = localModels.stream()
                .map(ModelWrapper::getLocalModel)
                .iterator();
        ModelWrapper model = new GlobalUpdateRunner(initParams, processingStage.processor(), processingStage.getStageIndex())
                .call(models)
                .next();

        // resend mining method to local nodes
        List<MiningMethod> methods = Collections.nCopies(nodeStubList.size(), model.getMiningMethod());
        List<ModelWrapper> localModels = sc.parallelize(methods, nodeStubList.size())
                .mapPartitions(new LocalFinalMethodCollector(initParams))
                .collect();
        verifyUniqueNodeExecutors(localModels);

        return ModelWrapper.global(GlobalMethodModelWrapper.of(model.getMiningMethod()), null);
    }

    private List<ModelWrapper> executeMethod() {
        List<ModelWrapper> executionAcks = sc.parallelize(nodeStubList, nodeStubList.size())
                .mapPartitions(new LocalExecutionRunner(initParams))
                .collect();
        verifyUniqueNodeExecutors(executionAcks);
        return executionAcks;
    }

    private void verifyUniqueNodeExecutors(List<ModelWrapper> models) {
        long uniqueAddresses = models.stream()
                .map(ModelWrapper::getAddress)
                .distinct()
                .count();
        Preconditions.checkState(uniqueAddresses == models.size(), "Not all nodes has been used during processing.");
    }

    private static InitParamsDto createInitParams(JsonArgsDto args) {
        return InitParamsDto.builder()
                .executionPath(args.getExecutionPath())
                .datasetsPath(args.getDatasetsPath())
                .trainDataId(args.getTrainDataId())
                .testDataId(args.getTestDataId())
                .executionId(args.getExecutionId())
                .algorithmPackageName(args.getAlgorithmPackageName())
                .distanceFunctionName(args.getDistanceFunctionName())
                .distanceFunctionPackageName(args.getDistanceFunctionPackageName())
                .executionParams(args.getExecutionParams())
                .build();
    }

}
