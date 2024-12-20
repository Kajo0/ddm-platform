package pl.edu.pw.ddm.platform.runner;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.spark.api.java.function.FlatMapFunction;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.LocalProcessor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.Processor;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.model.BaseModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;
import pl.edu.pw.ddm.platform.runner.data.NodeDataProvider;
import pl.edu.pw.ddm.platform.runner.data.NodeParamProvider;
import pl.edu.pw.ddm.platform.runner.models.ModelWrapper;
import pl.edu.pw.ddm.platform.runner.utils.AlgorithmProcessorInitializer;
import pl.edu.pw.ddm.platform.runner.utils.ModelPersister;
import pl.edu.pw.ddm.platform.runner.utils.PersistentIdStamper;
import pl.edu.pw.ddm.platform.runner.utils.TransferSizeUtil;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
class LocalProcessRunner implements FlatMapFunction<Iterator<Integer>, ModelWrapper> {

    private final InitParamsDto initParams;
    private final Class<? extends Processor> processor;
    private final int stageIndex;

    @Override
    public Iterator<ModelWrapper> call(Iterator<Integer> iterator) throws Exception {
        Integer id = iterator.next();
        PersistentIdStamper.save(id);

        NodeDataProvider dataProvider = new NodeDataProvider(initParams.getDatasetsPath(), initParams.getTrainDataId(), initParams.getTestDataId());
        ParamProvider paramProvider = new NodeParamProvider(initParams.findDistanceFunction(), initParams.getExecutionParams());

        LocalProcessor<LocalModel> lp = AlgorithmProcessorInitializer.initProcessor(
                initParams.getAlgorithmPackageName(),
                (Class<LocalProcessor<LocalModel>>) processor,
                LocalProcessor.class
        );

        System.out.println(paramProvider.prettyPrintParams());

        LocalDateTime start = LocalDateTime.now();
        LocalModel model = lp.processLocal(dataProvider, paramProvider);
        LocalDateTime end = LocalDateTime.now();

        ModelPersister.saveLocal(initParams.getExecutionPath(), model, stageIndex, initParams.getExecutionId());

        ModelWrapper wrapper = ModelWrapper.local(model, InetAddress.getLocalHost().toString(), id);
        wrapper.getTimeStatistics().setStart(start);
        wrapper.getTimeStatistics().setEnd(end);
        wrapper.getTimeStatistics().setDataLoadingMillis(dataProvider.getLoadingMillis());

        wrapper.getDatasetStatistics().setTrainingSamplesAmount(dataProvider.trainingSize());
        double avg = dataProvider.trainingSample10()
                .stream()
                .map(TransferSizeUtil::sizeOf)
                .mapToInt(i -> i)
                .average()
                .orElse(-1);
        wrapper.getDatasetStatistics().setAvgSampleSize((int) avg);
        Optional.of(model)
                .map(BaseModel::customMetrics)
                .ifPresent(wrapper.getDatasetStatistics()::setCustomMetrics);

        return new SingletonIterator(wrapper);
    }

}
