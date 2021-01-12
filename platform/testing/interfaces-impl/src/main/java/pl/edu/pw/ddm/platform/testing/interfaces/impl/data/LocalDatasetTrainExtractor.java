package pl.edu.pw.ddm.platform.testing.interfaces.impl.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Random;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.strategy.TempFileCreator;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class LocalDatasetTrainExtractor {

    private final Path dataPath;
    private final int percentage;
    private final Long seed;
    private final TempFileCreator fileCreator;

    private Random rand;

    @Getter
    private Path trainFile;

    @Getter
    private Path testFile;

    public void extract() throws IOException {
        if (processed()) {
            return;
        }
        checkOptions();
        init();

        trainFile = fileCreator.create("train");
        testFile = fileCreator.create("test");

        Files.lines(dataPath)
                .filter(StringUtils::isNotBlank)
                .forEach(this::processLine);
    }

    @SneakyThrows
    public void cleanup() {
        fileCreator.cleanup();
    }

    @SneakyThrows
    private void processLine(String line) {
        Path path = testFile;
        if (trainRandom()) {
            path = trainFile;
        }

        Files.write(path, (line + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
    }

    private boolean trainRandom() {
        return rand.nextInt(100) + 1 <= percentage;
    }

    private void checkOptions() {
        if (percentage <= 0 || percentage >= 100) {
            throw new IllegalArgumentException(
                    "Extract train percentage is value is out of range (0, 100) = " + percentage);
        }
    }

    private void init() {
        if (seed != null) {
            rand = new Random(seed);
        } else {
            rand = new Random();
        }
    }

    private boolean processed() {
        return trainFile != null && testFile != null;
    }

}
