package pl.edu.pw.ddm.platform.strategies.conceptdrift;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
public class ConceptDriftPartitionerStrategy implements PartitionerStrategy {

    private Random rand;
    private int workers;
    private int attrCount;
    private int drifts;
    private int discreteRanges;
    private String shiftLabel;

    private final Map<String, IdValuesPair> oneLabelData = new HashMap<>();

    private DriftBucketsPreparer.BucketedIds bucketedIdsForTestChart; // TODO remove as it is exposed only for test chart purposes

    @Override
    public String name() {
        return PartitionerStrategies.CONCEPT_DRIFT;
    }

    @Override
    public List<CustomParamDesc> availableParameters() {
        return List.of(
                CustomParamDesc.of("drifts",
                        Integer.class,
                        "On how many separation parts should divide data",
                        "2 - split on 2 separated sets",
                        "-1 - split on separated set for every node"),
                // TODO make it more sophisticated like to <0,1),<1,10> where cardinality is more equal
                CustomParamDesc.of("discreteRanges",
                        Integer.class,
                        "On how many ranges data should be discretized for numerical attributes",
                        "2 - eg. <0, 10> => <0, 5), <5, 10>",
                        "-1 - number of ranges is equal to log(max - min)"),
                CustomParamDesc.of("label",
                        Integer.class,
                        "Which class should be drifted, while other will be scattered with uniform distribution",
                        "A - samples with label 'A' will be drifted",
                        "null/novalueset - first class will be drifted")
        );
    }

    @Override
    public List<Path> partition(DataDesc dataDesc,
                                StrategyParameters strategyParameters,
                                PartitionFileCreator partitionFileCreator) throws IOException {
        readParams(dataDesc, strategyParameters);
        rand = prepareRandom(strategyParameters);

        prepareOneLabelData(dataDesc);
        var attrBuckets = prepareAttrBuckets(dataDesc);
        bucketedIdsForTestChart = attrBuckets;

        List<Path> tempFiles = partitionFileCreator.create(workers);
        // TODO handle already partitioned files
        var path = Path.of(Iterables.getOnlyElement(dataDesc.getFilesLocations()));
        Files.readAllLines(path)
                .forEach(l -> {
                    try {
                        var attrs = l.split(dataDesc.getSeparator());
                        var idx = attrs[dataDesc.getIdIndex()];
                        var label = attrs[dataDesc.getLabelIndex()];

                        int fileNumber;
                        if (shiftLabel.equals(label)) {
                            fileNumber = attrBuckets.bucketNumber(idx);
                        } else {
                            fileNumber = rand.nextInt(workers);
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

    private void prepareOneLabelData(DataDesc dataDesc) throws IOException {
        // TODO handle already partitioned files
        var path = Path.of(Iterables.getOnlyElement(dataDesc.getFilesLocations()));
        final int idCol = dataDesc.getIdIndex();
        final int labelCol = dataDesc.getLabelIndex();
        Files.readAllLines(path)
                .stream()
                .map(l -> l.split(dataDesc.getSeparator()))
                .filter(attrs -> shiftLabel.equals(attrs[labelCol]))
                .map(attrs -> new IdValuesPair(attrs[idCol], collectToArrayBut(attrs, idCol, labelCol)))
                .forEach(ip -> oneLabelData.put(ip.getId(), ip));

        if (oneLabelData.isEmpty()) {
            throw new IllegalArgumentException("No data samples with label '" + shiftLabel + "' found.");
        }
    }

    private String[] collectToArrayBut(String[] attrs, int idCol, int labelCol) {
        var result = new String[attrs.length - 2];
        var j = 0;
        for (int i = 0; i < attrs.length; ++i) {
            if (i != idCol && i != labelCol) {
                result[j++] = attrs[i];
            }
        }
        return result;
    }

    private DriftBucketsPreparer.BucketedIds prepareAttrBuckets(DataDesc dataDesc) {
        log.info("Discretizing numerical values to {} ranges.", discreteRanges);
        var numericAttrs = new boolean[attrCount];
        int j = 0;
        for (int i = 0; i < dataDesc.getColTypes().length; ++i) {
            if (i == dataDesc.getIdIndex() || i == dataDesc.getLabelIndex()) {
                continue;
            }
            numericAttrs[j++] = "numeric".equals(dataDesc.getColTypes()[i]);
        }

        var oneLabelDataDiscretized = oneLabelData;
        if (discreteRanges > 1) {

            oneLabelDataDiscretized = new DataDiscretizator(oneLabelData,
                    attrCount,
                    numericAttrs,
                    discreteRanges).process();
        }

        return new DriftBucketsPreparer(attrCount, drifts, oneLabelDataDiscretized, numericAttrs).prepare();
    }

    private void readParams(DataDesc dataDesc, StrategyParameters strategyParameters) {
        workers = strategyParameters.getPartitions();
        attrCount = dataDesc.getAttributesAmount();

        var params = Utils.simpleNumericParams(strategyParameters.getCustomParams());
        var strParams = Utils.simpleStringParams(strategyParameters.getCustomParams());

        discreteRanges = params.getOrDefault("discreteRanges", -1d).intValue();
        shiftLabel = strParams.getOrDefault("label", null);

        drifts = params.getOrDefault("drifts", -1d).intValue();
        if (drifts < 0) {
            drifts = -1;
        }

        Preconditions.checkState(drifts <= workers, "drifts must be <= to partitions");
        Preconditions.checkState(drifts > 1, "drifts must be greater than 1");
        Preconditions.checkNotNull(shiftLabel, "label param cannot be null");
    }

}
