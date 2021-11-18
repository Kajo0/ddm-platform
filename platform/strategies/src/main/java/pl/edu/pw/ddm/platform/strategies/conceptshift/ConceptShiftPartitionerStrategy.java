package pl.edu.pw.ddm.platform.strategies.conceptshift;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionFileCreator;
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy;
import pl.edu.pw.ddm.platform.strategies.PartitionerStrategies;
import pl.edu.pw.ddm.platform.strategies.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ConceptShiftPartitionerStrategy implements PartitionerStrategy {

    private Random rand;
    private int workers;
    private int shifts;
    private String shiftLabel;

    @Override
    public String name() {
        return PartitionerStrategies.CONCEPT_SHIFT;
    }

    @Override
    public List<CustomParamDesc> availableParameters() {
        return List.of(
                CustomParamDesc.of("shifts",
                        Integer.class,
                        "How many shifts to extract from label data set, while other data will be scattered with uniform distribution without label shift",
                        "1 - extract 1/workers of data for given label for the next node but with different label",
                        "2 - extract 2*(1/workers) of data for given label for the next two nodes but each with different label",
                        "-1 - shifts data with given label for every node separately but equally with uniform distribution"),
                CustomParamDesc.of("label",
                        Integer.class,
                        "Which class should be shifted, while other will be scattered with uniform distribution",
                        "A - samples with label 'A' will be shifted")
        );
    }

    @Override
    public List<Path> partition(DataDesc dataDesc,
                                StrategyParameters strategyParameters,
                                PartitionFileCreator partitionFileCreator) throws IOException {
        readParams(strategyParameters);
        rand = prepareRandom(strategyParameters);

        var oneLabelIds = prepareOneLabelData(dataDesc);
        var shiftedIds = new DataShifter(workers, shifts, shiftLabel, oneLabelIds, rand).shift();

        var tempFiles = partitionFileCreator.create(workers);
        // TODO handle already partitioned files
        var path = Path.of(Iterables.getOnlyElement(dataDesc.getFilesLocations()));
        Files.readAllLines(path)
                .forEach(l -> {
                    try {
                        var attrs = l.split(dataDesc.getSeparator());
                        var idx = attrs[dataDesc.getIdIndex()];
                        var label = attrs[dataDesc.getLabelIndex()];

                        var line = l;
                        int fileNumber;
                        if (shiftLabel.equals(label)) {
                            var nl = shiftedIds.get(idx);
                            fileNumber = nl.getNode();
                            if (!label.equals(nl.getLabel())) {
                                attrs[dataDesc.getLabelIndex()] = nl.getLabel();
                                line = String.join(dataDesc.getSeparator(), attrs);
                            }
                        } else {
                            fileNumber = rand.nextInt(workers);
                        }

                        Path tempFile = tempFiles.get(fileNumber);
                        Files.writeString(tempFile, line + System.lineSeparator(), StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        // TODO optimize
                        e.printStackTrace();
                    }
                });

        return tempFiles;
    }

    private List<String> prepareOneLabelData(DataDesc dataDesc) throws IOException {
        // TODO handle already partitioned files
        var path = Path.of(Iterables.getOnlyElement(dataDesc.getFilesLocations()));
        final int idCol = dataDesc.getIdIndex();
        final int labelCol = dataDesc.getLabelIndex();
        var result = Files.readAllLines(path)
                .stream()
                .map(l -> l.split(dataDesc.getSeparator()))
                .filter(attrs -> shiftLabel.equals(attrs[labelCol]))
                .map(attrs -> attrs[idCol])
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            throw new IllegalArgumentException("No data samples with label '" + shiftLabel + "' found.");
        }
        return result;
    }

    private void readParams(StrategyParameters strategyParameters) {
        workers = strategyParameters.getPartitions();

        var params = Utils.simpleNumericParams(strategyParameters.getCustomParams());
        var strParams = Utils.simpleStringParams(strategyParameters.getCustomParams());

        shiftLabel = strParams.getOrDefault("label", null);

        shifts = params.getOrDefault("shifts", -1d).intValue();
        if (shifts < 0) {
            shifts = workers;
        }

        Preconditions.checkState(shifts < workers, "shifts must be < to partitions");
        Preconditions.checkState(shifts > 0, "shifts must be greater than 0");
        Preconditions.checkNotNull(shiftLabel, "label param cannot be null");
    }

}
