package pl.edu.pw.ddm.platform.strategies;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Iterables;
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionFileCreator;
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy;

public class UniformPartitionerStrategy implements PartitionerStrategy {

    @Override
    public String name() {
        return PartitionerStrategies.UNIFORM;
    }

    @Override
    public List<Path> partition(DataDesc dataDesc, StrategyParameters strategyParameters, PartitionFileCreator partitionFileCreator) throws IOException {
        int workers = strategyParameters.getPartitions();
        Long samplesCount = dataDesc.getNumberOfSamples();
        if (samplesCount == null) {
            // TODO handle already partitioned files
            Path path = Path.of(Iterables.getOnlyElement(dataDesc.getFilesLocations()));
            samplesCount = (long) Files.readAllLines(path)
                    .size();
        }

        List<Path> tempFiles = partitionFileCreator.create(workers);

        int partSize = (int) (samplesCount / workers);
        int lastRest = (int) (samplesCount % workers);
        List<Integer> shuffleIndices = new ArrayList<>(samplesCount.intValue());
        for (int i = 0; i < lastRest; ++i) {
            shuffleIndices.add(0);
        }
        for (int i = 0; i < workers; ++i) {
            for (int j = 0; j < partSize; ++j) {
                shuffleIndices.add(i);
            }
        }
        Random rand = new Random();
        if (strategyParameters.getSeed() != null) {
            rand = new Random(strategyParameters.getSeed());
        }
        Collections.shuffle(shuffleIndices, rand);

        AtomicInteger i = new AtomicInteger(0);
        // TODO handle already partitioned files
        Path path = Path.of(Iterables.getOnlyElement(dataDesc.getFilesLocations()));
        Files.readAllLines(path)
                .forEach(l -> {
                    try {
                        int index = i.getAndIncrement();
                        int fileNumber = shuffleIndices.get(index);
                        Path tempFile = tempFiles.get(fileNumber);
                        Files.writeString(tempFile, l + System.lineSeparator(), StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        // TODO optimize
                        e.printStackTrace();
                    }
                });

        return tempFiles;
    }

}
