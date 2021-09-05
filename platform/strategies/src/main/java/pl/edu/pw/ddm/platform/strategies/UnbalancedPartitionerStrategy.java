package pl.edu.pw.ddm.platform.strategies;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionFileCreator;
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy;
import pl.edu.pw.ddm.platform.strategies.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class UnbalancedPartitionerStrategy implements PartitionerStrategy {

    private Random rand;
    private int workers;
    private Map<String, Long> labels;
    private boolean proportional;
    private int nodeThreshold;
    private double unbalancedness;

    private Map<String, Integer> largeAmount = new HashMap<>();
    private Map<String, Integer> smallAmount = new HashMap<>();
    private Map<String, Integer> restAmount = new HashMap<>();
    private Map<Integer, String> indexToLabel = new HashMap<>();
    private Map<Integer, Integer> indexToNode = new HashMap<>();

    @Override
    public String name() {
        return PartitionerStrategies.UNBALANCEDNESS;
    }

    @Override
    public List<CustomParamDesc> availableParameters() {
        return List.of(
                CustomParamDesc.of("proportional",
                        Boolean.class,
                        "whenever classes be scattered with equal proportions",
                        "0 - random",
                        "1 - proportional"),
                CustomParamDesc.of("unbalancedness",
                        Double.class,
                        "how big unbalancenes should be in range (0, 0.5>",
                        "0.1 - means that data amount ratio for small data to large node is 0.1"),
                CustomParamDesc.of("nodeThreshold",
                        Integer.class,
                        "threshold for nodes that will contain more and less amount of data",
                        "eg. '1' - if 2 nodes then 1. will contain more, 2. less data",
                        "eg. '2' - if 3 nodes then 1. and 2. will contain more, 3. less data",
                        "etc.")
        );
    }

    @Override
    public List<Path> partition(DataDesc dataDesc,
                                StrategyParameters strategyParameters,
                                PartitionFileCreator partitionFileCreator) throws IOException {
        readParams(dataDesc, strategyParameters);
        rand = prepareRandom(strategyParameters);

        Long samplesCount = dataDesc.getNumberOfSamples();
        if (samplesCount == null) {
            samplesCount = labels.values()
                    .stream()
                    .reduce(Long::sum)
                    .orElse(0L);
        }

        if (proportional) {
            calculateProportionalRatioAmounts();
        } else {
            calculateRatioAmounts(samplesCount);
        }

        List<Path> tempFiles = partitionFileCreator.create(workers);
        var indexToWorker = prepareIndexToWorker(samplesCount);

        var i = new AtomicInteger(0);
        // TODO handle already partitioned files
        var path = Path.of(Iterables.getOnlyElement(dataDesc.getFilesLocations()));
        Files.readAllLines(path)
                .forEach(l -> {
                    try {
                        int index = i.getAndIncrement();
                        int fileNumber;
                        if (!proportional) {
                            fileNumber = indexToWorker.get(index);
                        } else {
                            var idx = l.split(dataDesc.getSeparator())[dataDesc.getIdIndex()];
                            fileNumber = indexToNode.get(Integer.valueOf(idx));
                        }

                        Path tempFile = tempFiles.get(fileNumber);
                        Files.writeString(tempFile, l + System.lineSeparator(), StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        // TODO optimize
                        e.printStackTrace();
                    }
                });

        return tempFiles;
    }

    private List<Integer> prepareIndexToWorker(long samplesCount) {
        if (!proportional) {
            var indexToWorker = new ArrayList<Integer>((int) samplesCount);
            // put 'math rest' to the first node
            for (int i = 0; i < restAmount.get(""); ++i) {
                indexToWorker.add(0);
            }
            for (int i = 0; i < nodeThreshold; ++i) {
                for (int j = 0; j < largeAmount.get(""); ++j) {
                    indexToWorker.add(i);
                }
            }
            for (int i = nodeThreshold; i < workers; ++i) {
                for (int j = 0; j < smallAmount.get(""); ++j) {
                    indexToWorker.add(i);
                }
            }

            Collections.shuffle(indexToWorker, rand);
            return indexToWorker;
        } else {
            var labelIndices = new HashMap<String, List<Integer>>();
            indexToLabel.forEach(
                    (index, label) -> labelIndices.computeIfAbsent(label, s -> new ArrayList<>())
                            .add(index)
            );
            labelIndices.values()
                    .forEach(c -> Collections.shuffle(c, rand));

            for (String l : labelIndices.keySet()) {
                for (int i = 0; i < nodeThreshold; ++i) {
                    for (int j = 0; j < largeAmount.get(l); ++j) {
                        var index = labelIndices.get(l)
                                .remove(0);
                        indexToNode.put(index, i);
                    }
                }
                for (int i = nodeThreshold; i < workers; ++i) {
                    for (int j = 0; j < smallAmount.get(l); ++j) {
                        var index = labelIndices.get(l)
                                .remove(0);
                        indexToNode.put(index, i);
                    }
                }
                // put 'math rest' to the first node
                for (int i = 0; i < restAmount.get(l); ++i) {
                    var index = labelIndices.get(l)
                            .remove(0);
                    indexToNode.put(index, 0);
                }
            }
            return null;
        }
    }

    private void calculateProportionalRatioAmounts() {
        largeAmount = new HashMap<>();
        smallAmount = new HashMap<>();
        restAmount = new HashMap<>();

        labels.forEach((label, count) -> {
            int large = countLargeAmount(count);
            int small = countSmallAmount(count, large);
            int rest = countRestAmount(count, large, small);
            largeAmount.put(label, large);
            smallAmount.put(label, small);
            restAmount.put(label, rest);

            Preconditions.checkState(large * nodeThreshold + small * (workers - nodeThreshold) + rest == count,
                    "error during proportional ratio calculations");
            Preconditions.checkState(large > 0, "large amount is 0, cannot assign data using provided params");
            Preconditions.checkState(small > 0, "small amount is 0, cannot assign data using provided params");

            log.info("Obtained '{}' ratio of target '{}' (label='{}') for given parameters: [data={},workers={}]",
                    (double) small / large, unbalancedness, label, count, workers);
        });
    }

    private void calculateRatioAmounts(long samplesCount) {
        int large = countLargeAmount(samplesCount);
        int small = countSmallAmount(samplesCount, large);
        int rest = countRestAmount(samplesCount, large, small);
        largeAmount = Map.of("", large);
        smallAmount = Map.of("", small);
        restAmount = Map.of("", rest);

        Preconditions.checkState(large * nodeThreshold + small * (workers - nodeThreshold) + rest == samplesCount,
                "error during ratio calculations");
        Preconditions.checkState(large > 0, "large amount is 0, cannot assign data using provided params");
        Preconditions.checkState(small > 0, "small amount is 0, cannot assign data using provided params");

        log.info("Obtained '{}' ratio of target '{}' for given parameters: [data={},workers={}]",
                (double) small / large, unbalancedness, samplesCount, workers);
    }

    /**
     * Using system of equations for large/small calculation:
     * { nodeThreshold * largeAmount + (workers - nodeThreshold) * smallAmount = samplesCount
     * { smallAmount / largeAmount = unbalancedness
     */
    private int countLargeAmount(long samplesCount) {
        return (int) (samplesCount / (nodeThreshold + workers * unbalancedness - nodeThreshold * unbalancedness));
    }

    private int countSmallAmount(long samplesCount, int largeAmount) {
        return (int) (samplesCount - largeAmount * nodeThreshold) / (workers - nodeThreshold);
    }

    private int countRestAmount(long samplesCount, int largeAmount, int smallAmount) {
        return (int) (samplesCount - largeAmount * nodeThreshold - smallAmount * (workers - nodeThreshold));
    }

    @SneakyThrows
    private void readParams(DataDesc dataDesc, StrategyParameters strategyParameters) {
        workers = strategyParameters.getPartitions();

        var params = Utils.simpleNumericParams(strategyParameters.getCustomParams());
        proportional = params.getOrDefault("proportional", 0d) != 0d;
        nodeThreshold = params.getOrDefault("nodeThreshold", (double) (workers / 2)).intValue();
        unbalancedness = params.getOrDefault("unbalancedness", 0.5);

        if (proportional) {
            indexToLabel = Utils.mapIndexToLabel(dataDesc);
            labels = indexToLabel.values()
                    .stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        } else {
            labels = Utils.countLabels(dataDesc);
        }

        Preconditions.checkState(workers > nodeThreshold, "nodeThreshold must be less than node amount");
        Preconditions.checkState(nodeThreshold > 0, "nodeThreshold cannot be 0");
        Preconditions.checkState(unbalancedness > 0, "unbalancedness cannot be <= 0");
        Preconditions.checkState(unbalancedness <= 0.5, "unbalancedness cannot be > 0.5");
    }

}
