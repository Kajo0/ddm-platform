package pl.edu.pw.ddm.platform.testing.interfaces.impl;

import java.util.List;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import pl.edu.pw.ddm.platform.interfaces.algorithm.DdmPipeline;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.Processor;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ResultCollector;
import pl.edu.pw.ddm.platform.interfaces.mining.Classifier;
import pl.edu.pw.ddm.platform.interfaces.mining.Clustering;
import pl.edu.pw.ddm.platform.interfaces.mining.MiningMethod;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.NodeDataProvider;
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.NodeParamProvider;
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.NodeResultCollector;
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.NodeSampleProvider;

public class DdmPipelineRunner {

    private final DdmPipeline ddmPipeline;
    private final ParamProvider paramProvider;
    private final ResultCollector resultCollector;

    private final DdmMasterNodeRunner masterNodeRunner;
    private final List<DdmNodeRunner> nodeRunners;

    private List<LocalModel> localModels;
    private GlobalModel globalModel;
    private List<MiningMethod> miningMethods;

    @SneakyThrows
    public DdmPipelineRunner(DdmExecutionConfig config) {
        this.ddmPipeline = config.getAlgorithmConfig()
                .pipeline();
        this.paramProvider = new NodeParamProvider(
                config.getDistanceFunction(),
                config.getExecutionParams()
        );
        this.resultCollector = new NodeResultCollector();

        this.masterNodeRunner = new DdmMasterNodeRunner(paramProvider);
        this.nodeRunners = config.getDataPath()
                .stream()
                .map(dataPath -> buildNodeRunner(dataProvider(dataPath, config)))
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private DdmNodeRunner buildNodeRunner(DataProvider dataProvider) {
        return new DdmNodeRunner(dataProvider, paramProvider);
    }

    @SneakyThrows
    private DataProvider dataProvider(String dataPath, DdmExecutionConfig config) {
        DataProvider.DataDesc desc = DataProvider.DataDesc.builder()
                .separator(config.getSeparator())
                .idIndex(config.getIdIndex())
                .labelIndex(config.getLabelIndex())
                .attributesAmount(config.getAttributesAmount())
                .colTypes(config.getColTypes())
                .build();
        return new NodeDataProvider(dataPath, config.getTestDataPath(), desc, true);
    }

    public NodeResultCollector run() {
        runInternal();
        return (NodeResultCollector) resultCollector;
    }

    private void runInternal() {
        ddmPipeline.getStages()
                .forEach(this::processStage);

        executeMethods();
    }

    @SneakyThrows
    private void processStage(DdmPipeline.ProcessingStage processingStage) {
        Class<? extends Processor> processor = processingStage.processor();
        DdmPipeline.Stage stage = processingStage.getStage();

        switch (stage) {
            case LOCAL:
                localModels = nodeRunners.stream()
                        .peek(n -> n.setLocalProcessor(newInstance(processor)))
                        .map(DdmNodeRunner::processLocal)
                        .collect(Collectors.toList());
                return;
            case GLOBAL:
                masterNodeRunner.setGlobalProcessor(newInstance(processor));
                globalModel = masterNodeRunner.processGlobal(localModels);
                return;
            case LOCAL_REPEAT:
                localModels = nodeRunners.stream()
                        .peek(n -> n.setLocalRepeater(newInstance(processor)))
                        .map(n -> n.repeatLocal(globalModel))
                        .collect(Collectors.toList());
                return;
            case GLOBAL_UPDATE:
                masterNodeRunner.setGlobalUpdater(newInstance(processor));
                MiningMethod miningMethod = masterNodeRunner.updateGlobal(localModels);
                miningMethods = nodeRunners.stream()
                        .map(n -> miningMethod)
                        .collect(Collectors.toList());
                return;
            case LOCAL_UPDATE:
                miningMethods = nodeRunners.stream()
                        .peek(n -> n.setLocalUpdater(newInstance(processor)))
                        .map(n -> n.updateLocal(globalModel))
                        .collect(Collectors.toList());
                return;
            default:
                throw new UnsupportedOperationException("Unknown DDM pipeline processing stage");
        }
    }

    @SneakyThrows
    private <T> T newInstance(Class<? extends Processor> processor) {
        return (T) processor.getConstructor().newInstance();
    }

    private void executeMethods() {
        MiningMethod firstMethod = miningMethods.get(0);
        if (firstMethod instanceof Classifier) {
            DdmNodeRunner firstNode = nodeRunners.get(0);
            ((Classifier) firstMethod).classify(
                    NodeSampleProvider.fromData(firstNode.getDataProvider().test()),
                    paramProvider,
                    resultCollector
            );
        } else if (firstMethod instanceof Clustering) {
            nodeRunners.forEach(n ->
                    ((Clustering) n.getMiningMethod()).cluster(
                            NodeSampleProvider.fromData(n.getDataProvider().training()),
                            paramProvider,
                            resultCollector
                    ));
        } else {
            throw new IllegalArgumentException("Unknown mining method.");
        }
    }

}
