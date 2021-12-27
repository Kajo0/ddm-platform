package ddm.samples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Doubles;
import lombok.extern.slf4j.Slf4j;
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionFileCreator;
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy;
import pl.edu.pw.ddm.platform.strategies.UniformPartitionerStrategy;

@Slf4j
public class DenseAndOutliersStrategy implements PartitionerStrategy {

    static {
        // TODO FIXME required as current classloader does not load used transitive classes
        var denseExtractorClass = DenseExtractor.class;
        var dataIdWrapperClass = DenseExtractor.DataIdWrapper.class;
        var pairClass = DenseExtractor.Pair.class;
        var nodeDataProviderClass = NodeDataProvider.class;
        var nodeDataClass = NodeData.class;
        var numericCenterFinderClass = NumericCenterFinder.class;
    }

    @Override
    public String name() {
        return "dense-and-outliers";
    }

    @Override
    public List<Path> partition(DataDesc dataDesc, StrategyParameters strategyParameters, PartitionFileCreator partitionFileCreator) throws IOException {
        int workers = strategyParameters.getPartitions();
        Preconditions.checkState(workers >= 2, "Strategy not applicable for less than 2 nodes.");

        DistanceFunction func = strategyParameters.getDistanceFunction();
        Preconditions.checkNotNull(func, "Distance function cannot be null.");

        Double densePercent = Doubles.tryParse(strategyParameters.getCustomParams());
        Preconditions.checkNotNull(densePercent, "'densePercent' is not number: '%s'", strategyParameters.getCustomParams());
        Preconditions.checkState(densePercent > 0 && densePercent < 1, "'densePercent' value '%s' outside range (0, 1)", densePercent);

        // TODO handle already partitioned
        Path path = Path.of(Iterables.getOnlyElement(dataDesc.getFilesLocations()));
        NodeDataProvider dataProvider = new NodeDataProvider(path.toString(), null, mapDesc(dataDesc));
        Collection<Data> data = dataProvider.all();
        log.info("{} data to divide.", data.size());

        Map<String, List<Data>> groupped = data.stream()
                .collect(Collectors.groupingBy(Data::getLabel));

        var dense = new HashMap<String, List<Data>>();
        var outliers = new HashMap<String, List<Data>>();
        for (var entry : groupped.entrySet()) {
            var extract = new DenseExtractor(entry.getValue(), func, densePercent).extract();
            dense.put(entry.getKey(), extract.getDense());
            outliers.put(entry.getKey(), extract.getOutliers());
        }

        List<Path> tempFiles = partitionFileCreator.create(2);
        write(tempFiles.get(0), dense, dataDesc);
        write(tempFiles.get(1), outliers, dataDesc);

        if (workers == 2) {
            return tempFiles;
        } else {
            var uniform = new UniformPartitionerStrategy();
            var fl = dataDesc.getFilesLocations(); // TODO deep copy check

            dataDesc.getFilesLocations().clear();
            dataDesc.getFilesLocations().add(tempFiles.get(0).toAbsolutePath().toString());
            strategyParameters.setPartitions(workers / 2 + workers % 2);
            var first = uniform.partition(dataDesc, strategyParameters, partitionFileCreator);

            dataDesc.getFilesLocations().clear();
            dataDesc.getFilesLocations().add(tempFiles.get(1).toAbsolutePath().toString());
            strategyParameters.setPartitions(workers / 2);
            var second = uniform.partition(dataDesc, strategyParameters, partitionFileCreator);

            strategyParameters.setPartitions(workers);
            dataDesc.setFilesLocations(fl);

            return Stream.of(first, second)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
    }

    private void write(Path path, HashMap<String, List<Data>> data, DataDesc dataDesc) {
        data.values()
                .stream()
                .flatMap(Collection::stream)
                .forEach(d -> {
                    try {
                        Files.writeString(path, toLIneWithEndline(d, dataDesc), StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        // TODO optimize
                        e.printStackTrace();
                    }
                });
    }

    private String toLIneWithEndline(Data data, DataDesc dataDesc) {
        StringBuilder line = new StringBuilder();
        int minus = 0;
        int cols = data.getAttributes().length + 2;
        for (int i = 0; i < cols; ++i) {
            if (i == dataDesc.getIdIndex()) {
                line.append(data.getId());
                ++minus;
            } else if (i == dataDesc.getLabelIndex()) {
                line.append(data.getLabel());
                ++minus;
            } else {
                line.append(data.getAttribute(i - minus));
            }
            if (i + 1 != cols) {
                line.append(dataDesc.getSeparator());
            }
        }
        return line.append(System.lineSeparator())
                .toString();
    }

    private DataProvider.DataDesc mapDesc(DataDesc desc) {
        DataProvider.DataDesc.DataDescBuilder dataDesc = DataProvider.DataDesc.builder()
                .separator(desc.getSeparator())
                .idIndex(desc.getIdIndex())
                .labelIndex(desc.getLabelIndex())
                .colTypes(desc.getColTypes())
                .attributesAmount(desc.getAttributesAmount());

        String[] colTypes = desc.getColTypes();
        if (colTypes != null) {
            dataDesc.colTypes(Arrays.copyOf(colTypes, colTypes.length));
        }

        return dataDesc.build();
    }

}
