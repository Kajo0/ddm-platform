package pl.edu.pw.ddm.platform.strategies;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionFileCreator;
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy;

@Slf4j
public class SeparateByLabelsPartitionerStrategy implements PartitionerStrategy {

    private static final String BINDING_SIGN = "|";
    private static final String SEPARATION_SIGN = ",";

    @Override
    public String name() {
        return PartitionerStrategies.SEPARATE_LABELS;
    }

    @Override
    public List<Path> partition(DataDesc dataDesc, StrategyParameters strategyParameters, PartitionFileCreator partitionFileCreator) throws IOException {
        int workers = strategyParameters.getPartitions();

        Map<String, Integer> labelToWorker = mapLabelToWorker(dataDesc, workers, strategyParameters.getCustomParams());
        List<Path> tempFiles = partitionFileCreator.create(workers);

        // TODO handle already partitioned
        Path path = Path.of(Iterables.getOnlyElement(dataDesc.getFilesLocations()));
        Files.readAllLines(path)
                .forEach(l -> {
                    try {
                        String label = l.split(dataDesc.getSeparator())[dataDesc.getLabelIndex()];
                        int fileNumber = labelToWorker.get(label);

                        Path tempFile = tempFiles.get(fileNumber);
                        Files.writeString(tempFile, l + System.lineSeparator(), StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        // TODO optimize
                        e.printStackTrace();
                    }
                });

        return tempFiles;
    }

    private Map<String, Integer> mapLabelToWorker(DataDesc dataDesc, int workers, String params) throws IOException {
        Map<String, Long> labelCount = countLabels(dataDesc);
        log.info("Mapping {} labels to {} workers with params '{}'.", labelCount.size(), workers, params);

        Map<String, Integer> mapping = new HashMap<>(labelCount.size());
        if (labelCount.size() <= workers && params == null) {
            int[] i = new int[]{0};
            labelCount.forEach((label, count) -> mapping.put(label, i[0]++));
        } else if (params != null) {
            Map<Integer, String[]> workerToLabels = evaluateParams(params, workers, labelCount.keySet());
            workerToLabels.forEach((worker, labels) -> {
                for (String l : labels) {
                    mapping.put(l, worker);
                }
            });
        } else {
            throw new IllegalStateException("More labels than workers in instance and no mapping provided.");
        }

        long nodeMapping = mapping.values()
                .stream()
                .distinct()
                .count();
        if (nodeMapping < workers) {
            log.warn("Only {} of {} workers will have data", nodeMapping, workers);
        }

        return mapping;
    }

    // TODO optimize
    private Map<String, Long> countLabels(DataDesc dataDesc) throws IOException {
        // TODO handle already partitioned
        Path path = Path.of(Iterables.getOnlyElement(dataDesc.getFilesLocations()));

        return Files.readAllLines(path)
                .stream()
                .map(l -> l.split(dataDesc.getSeparator()))
                .collect(Collectors.groupingBy(d -> d[dataDesc.getLabelIndex()], Collectors.counting()));
    }

    private Map<Integer, String[]> evaluateParams(String params, int workers, Set<String> labels) {
        String[] separated = params.split(SEPARATION_SIGN);
        if (separated.length > workers) {
            throw new IllegalArgumentException("Not enough workers to divide data.");
        }

        Map<Integer, String[]> mapping = new HashMap<>(workers);
        for (int i = 0; i < separated.length; ++i) {
            String[] bindLabels = separated[i].split(Pattern.quote(BINDING_SIGN));
            for (String l : bindLabels) {
                if (!labels.contains(l)) {
                    throw new IllegalStateException("Provided unknown label: " + l);
                }
            }
            mapping.put(i, bindLabels);
        }
        long bindLabelsCount = mapping.values()
                .stream()
                .map(Arrays::asList)
                .mapToLong(Collection::size)
                .sum();
        Preconditions.checkState(bindLabelsCount == labels.size(), "Provided labels does not match all or exceeds real labels count.");

        return mapping;
    }

}
