package pl.edu.pw.ddm.platform.core.data.strategy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class UniformDataPartitioner implements PartitionerStrategy {

    @Override
    public List<Path> partition(File dataFile, int workers, long samplesCount) throws IOException {
        List<Path> tempFiles = IntStream.range(0, workers)
                .mapToObj(wi -> {
                    try {
                        return Files.createTempFile("splitter.", String.valueOf(wi));
                    } catch (IOException e) {
                        // TODO optimize
                        e.printStackTrace();
                        return Path.of("/tmp/splitter. " + wi);
                    }
                })
                .collect(Collectors.toList());

        int partSize = (int) (samplesCount / workers);
        int lastRest = (int) (samplesCount % workers);
        List<Integer> shuffleIndices = new ArrayList<>((int) samplesCount);
        for (int i = 0; i < lastRest; ++i) {
            shuffleIndices.add(0);
        }
        for (int i = 0; i < workers; ++i) {
            for (int j = 0; j < partSize; ++j) {
                shuffleIndices.add(i);
            }
        }
        Collections.shuffle(shuffleIndices);

        AtomicInteger i = new AtomicInteger(0);
        Files.readAllLines(dataFile.toPath())
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
