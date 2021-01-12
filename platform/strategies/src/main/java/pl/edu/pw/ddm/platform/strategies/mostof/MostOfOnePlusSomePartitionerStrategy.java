package pl.edu.pw.ddm.platform.strategies.mostof;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionFileCreator;
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy;
import pl.edu.pw.ddm.platform.strategies.PartitionerStrategies;
import pl.edu.pw.ddm.platform.strategies.utils.Utils;

@Slf4j
public class MostOfOnePlusSomePartitionerStrategy implements PartitionerStrategy {

    @Override
    public String name() {
        return PartitionerStrategies.SEPARATE_LABELS;
    }

    @Override
    public List<Path> partition(DataDesc dataDesc, StrategyParameters strategyParameters,
            PartitionFileCreator partitionFileCreator) throws IOException {
        var labels = Utils.countLabels(dataDesc);
        var params = Utils.simpleNumericParams(strategyParameters.getCustomParams());

        var labelsPreparer = new ScatteringLabelsPreparer(strategyParameters.getPartitions(), labels.keySet()
                .size(), params.getOrDefault("additionalClassesNumber", 0d)
                .intValue(), params.getOrDefault("emptyWorkerFill", 0d)
                .intValue());
        var labelScattering = labelsPreparer.prepare();
        log.info("Prepared labels scattering: '{}'.", labelScattering);

        var amountsPreparer =
                new ScatteringAmountsPreparer(labels, labelScattering, params.getOrDefault("fillEmptyButPercent", 0.8),
                        params.getOrDefault("additionalClassesPercent", 0.05));
        var amountScattering = amountsPreparer.prepare();
        log.info("Prepared amounts scattering: '{}'.", amountScattering);

        int workers = strategyParameters.getPartitions();
        Random rand;
        if (strategyParameters.getSeed() != null) {
            rand = new Random(strategyParameters.getSeed());
        } else {
            rand = new Random();
        }

        List<Path> tempFiles = partitionFileCreator.create(workers);
        Path path = Path.of(Iterables.getOnlyElement(dataDesc.getFilesLocations()));
        Files.readAllLines(path)
                .forEach(l -> {
                    try {
                        String label = l.split(dataDesc.getSeparator())[dataDesc.getLabelIndex()];
                        int fileNumber;

                        boolean again;
                        do {
                            fileNumber = rand.nextInt(workers);
                            again = amountScattering.get(fileNumber, label) <= 0;
                            if (!again) {
                                amountScattering.decrease(fileNumber, label);
                            }
                        } while (again);

                        Path tempFile = tempFiles.get(fileNumber);
                        Files.writeString(tempFile, l + System.lineSeparator(), StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        // TODO optimize
                        e.printStackTrace();
                    }
                });
        log.info("Amount scattering is empty now? = '{}' of scattering -> '{}'.", amountScattering.isEmpty(),
                amountScattering);

        return tempFiles;
    }

}
