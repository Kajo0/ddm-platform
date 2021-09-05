package pl.edu.pw.ddm.platform.strategies.covariateshift;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionFileCreator;
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy;
import pl.edu.pw.ddm.platform.strategies.PartitionerStrategies;
import pl.edu.pw.ddm.platform.strategies.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class CovariateShiftPartitionerStrategy implements PartitionerStrategy {

    private Random rand;
    private int workers;
    private long samplesCount;
    private double shift;
    private int splits;
    private int method;
    private int attribute;

    private final List<IdValuePair> idToAttr = new ArrayList<>();
    private Map<Integer, List<IdValuePair>> splitPairs;
    private Map<Integer, List<String>> splitToId = new HashMap<>();

    @Override
    public String name() {
        return PartitionerStrategies.COVARIATE_SHIFT;
    }

    @Override
    public List<CustomParamDesc> availableParameters() {
        return List.of(
                // TODO add more features/shifts eg. 0.1|0.2 for features 2|4
                CustomParamDesc.of("shift", Double.class, "shift ratio between next splits with range (0, 0.5>"),
                CustomParamDesc.of("splits",
                        Integer.class,
                        "how split data attribute distributions among nodes",
                        "-1 - equal/disjunction split on every node eg. for 4 nodes 1,2|3,4|5,6|7,8",
                        "2 - split on two parts with one border like 1,2|3,4",
                        "3 - split on 3 equal parts with two borders like 1,2,3|4,5,6|7,8,9"),
                CustomParamDesc.of("method",
                        Integer.class,
                        "which split method to use",
                        "0 - EQUAL_SPLIT_RANDOM_WITH_ROLLBACK",
                        "1 - EQUAL_SPLIT_RANDOM",
                        "2 - EQUAL_SPLIT_BORDER",
                        "3 - RANDOM_SPLIT_RANDOM_WITH_ROLLBACK",
                        "4 - RANDOM_SPLIT_RANDOM",
                        "5 - RANDOM_SPLIT_BORDER"),
                CustomParamDesc.of("attribute",
                        Integer.class,
                        "attribute column number to shift excluding id and label",
                        "0 - means first attribute column",
                        "1 - means second attribute column",
                        "1 - means second column 'col1' when: col0,col1,label",
                        "1 - means third column 'col1' when: id,col0,col1,label",
                        "1 - means fourth column 'col1' when: id,col0,label,col1")
        );
    }

    @Override
    public List<Path> partition(DataDesc dataDesc,
                                StrategyParameters strategyParameters,
                                PartitionFileCreator partitionFileCreator) throws IOException {
        readParams(dataDesc, strategyParameters);
        rand = prepareRandom(strategyParameters);

        collectAndSortValues(dataDesc);
        splitValues();

        var splitMap = new HashMap<Integer, SplitMapEntry>();
        for (int i = 0; i < splits; ++i) {
            splitMap.put(i, new SplitMapEntry(0, 0, 0));
        }
        if (workers > splits) {
            for (int i = 0; i < workers; ++i) {
                var entry = splitMap.get(i % splits);
                entry.setModulo(entry.getModulo() + 1);
            }

            var counter = new AtomicInteger(0);
            splitMap.forEach((k, v) -> v.setShift(counter.getAndAdd(v.getModulo())));
        } else {
            for (int i = 0; i < splits; ++i) {
                var entry = splitMap.get(i);
                entry.setModulo(i % workers);
                entry.setShift(i % workers);
            }
        }

        List<Path> tempFiles = partitionFileCreator.create(workers);
        // TODO handle already partitioned files
        var path = Path.of(Iterables.getOnlyElement(dataDesc.getFilesLocations()));
        Files.readAllLines(path)
                .forEach(l -> {
                    try {
                        var idx = l.split(dataDesc.getSeparator())[dataDesc.getIdIndex()];
                        var splitKey = splitToId.entrySet()
                                .stream()
                                .filter(entry -> entry.getValue().contains(idx))
                                .map(Map.Entry::getKey)
                                .findFirst()
                                .orElseThrow();
                        splitToId.get(splitKey)
                                .remove(idx);

                        var entry = splitMap.get(splitKey);
                        var lastPut = entry.getLastPut();
                        var newLastPut = lastPut;
                        if (workers > splits) {
                            newLastPut = (lastPut + 1) % entry.getModulo();
                        }
                        entry.setLastPut(newLastPut);

                        int fileNumber = lastPut + entry.getShift();

                        Path tempFile = tempFiles.get(fileNumber);
                        Files.writeString(tempFile, l + System.lineSeparator(), StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        // TODO optimize
                        e.printStackTrace();
                    }
                });

        return tempFiles;
    }

    private void collectAndSortValues(DataDesc dataDesc) throws IOException {
        // TODO handle already partitioned files
        var path = Path.of(Iterables.getOnlyElement(dataDesc.getFilesLocations()));
        final int idCol = dataDesc.getIdIndex();
        final int attrCol = getAttributeCol(dataDesc);
        Files.readAllLines(path)
                .stream()
                .map(l -> l.split(dataDesc.getSeparator()))
                .map(attrs -> new IdValuePair(attrs[idCol], attrs[attrCol]))
                .sorted(Comparator.comparing(IdValuePair::getValue))
                .forEach(idToAttr::add);
    }

    private void splitValues() {
        splitPairs = new CovariateShifter(idToAttr, samplesCount, shift, splits, rand, method)
                .shift();
        splitPairs.values()
                .forEach(list -> Collections.shuffle(list, rand));
        splitPairs.forEach((k, v) -> {
            var list = splitToId.computeIfAbsent(k, s -> new ArrayList<>());
            v.stream()
                    .map(IdValuePair::getId)
                    .forEach(list::add);
        });
    }

    private void readParams(DataDesc dataDesc, StrategyParameters strategyParameters) {
        workers = strategyParameters.getPartitions();
        samplesCount = dataDesc.getNumberOfSamples();

        var params = Utils.simpleNumericParams(strategyParameters.getCustomParams());
        shift = params.getOrDefault("shift", 0d);
        splits = params.getOrDefault("splits", -1d).intValue();
        attribute = params.getOrDefault("attribute", -1d).intValue();
        method = params.getOrDefault("method", 0d).intValue();

        Preconditions.checkState(splits == -1 || splits > 1, "splits amount must be greater than 1 or equal to -1");
        if (splits == -1) {
            splits = Math.max(workers / 2, 2);
        }
        Preconditions.checkState(shift > 0 && shift <= 0.5, "shift must be in range (0, 0.5>");

        int attrCol = getAttributeCol(dataDesc);
        Preconditions.checkState(attribute >= 0, "attribute col must be provided");
        Preconditions.checkState(attribute < dataDesc.getAttributesAmount(),
                "attribusplitte exceed attribute count");
        Preconditions.checkState(attrCol != dataDesc.getLabelIndex(), "attribute must not point to label column");

        var maxMethodOrdinal = CovariateShifter.SplitMethods.values().length - 1;
        Preconditions.checkState(method >= 0 && method <= maxMethodOrdinal,
                "split method out of bounds, range is <0, %s>.", maxMethodOrdinal);

        int cols = dataDesc.getAttributesAmount();
        if (dataDesc.getIdIndex() != null) {
            ++cols;
        }
        if (dataDesc.getLabelIndex() != null) {
            ++cols;
        }
        Preconditions.checkState(attrCol < cols, "attribute exceed column number");
    }

    /**
     * Identifier index is omitted e.g.
     * <pre>
     * I,1,2,3,L -> L = 3
     * L,1,2,3,I -> L = 0
     * 1,2,I,L,3 -> L = 2
     * 1,2,L,I,3 -> L = 2
     * </pre>
     */
    private int getAttributeCol(DataDesc dataDesc) {
        var result = attribute;
        if (dataDesc.getIdIndex() <= result) {
            ++result;
        }
        if (dataDesc.getLabelIndex() <= result) {
            ++result;
        }
        return result;
    }

}
