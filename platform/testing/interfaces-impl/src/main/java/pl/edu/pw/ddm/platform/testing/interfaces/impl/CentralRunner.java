package pl.edu.pw.ddm.platform.testing.interfaces.impl;

import java.util.List;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import pl.edu.pw.ddm.platform.interfaces.algorithm.GlobalProcessor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.LocalProcessor;
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

public class CentralRunner {

    private final GlobalProcessor globalProcessor;
    private final ParamProvider paramProvider;
    private final ResultCollector resultCollector;

    private final List<NodeRunner> nodeRunners;

    @SneakyThrows
    public CentralRunner(ExecutionConfig config) {
        this.globalProcessor = config.getGlobalProcessor().getClass().getDeclaredConstructor().newInstance();
        this.paramProvider = new NodeParamProvider(
                config.getDistanceFunction(),
                config.getExecutionParams()
        );
        this.resultCollector = new NodeResultCollector();

        this.nodeRunners = config.getDataPath()
                .stream()
                .map(dataPath -> buildNodeRunner(dataProvider(dataPath, config), config.getLocalProcessor()))
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private NodeRunner buildNodeRunner(DataProvider dataProvider, LocalProcessor localProcessor) {
        return new NodeRunner(
                localProcessor.getClass().getDeclaredConstructor().newInstance(),
                dataProvider,
                paramProvider
        );
    }

    private DataProvider dataProvider(String dataPath, ExecutionConfig config) {
        return new NodeDataProvider(
                dataPath,
                config.getTestDataPath(),
                config.getSeparator(),
                config.getIdIndex(),
                config.getLabelIndex(),
                config.getAttributesAmount(),
                config.getColTypes()
        );
    }

    public NodeResultCollector run() {
        runInternal();
        return (NodeResultCollector) resultCollector;
    }

    private void runInternal() {
        List<LocalModel> localModels = processLocal();

        GlobalModel globalModel = processGlobal(localModels);

        List<MiningMethod> methods = updateLocal(globalModel);

        executeMethod(methods);
    }

    private List<LocalModel> processLocal() {
        return nodeRunners.stream()
                .map(NodeRunner::processLocal)
                .collect(Collectors.toList());
    }

    private GlobalModel processGlobal(List<LocalModel> localModels) {
        return globalProcessor.processGlobal(localModels, paramProvider);
    }

    private List<MiningMethod> updateLocal(GlobalModel globalModel) {
        return nodeRunners.stream()
                .map(n -> n.updateLocal(globalModel))
                .collect(Collectors.toList());
    }

    private void executeMethod(List<MiningMethod> methods) {
        MiningMethod firstMethod = methods.get(0);
        if (firstMethod instanceof Classifier) {
            NodeRunner firstNode = nodeRunners.get(0);
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
