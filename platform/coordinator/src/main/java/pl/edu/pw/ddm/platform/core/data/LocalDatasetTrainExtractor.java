package pl.edu.pw.ddm.platform.core.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import java.util.function.Predicate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class LocalDatasetTrainExtractor {

    // TODO wrap in wrapper
    private final Path dataPath;
    private final String separator;
    private final Integer labelIndex; // TODO maybe in the future for uniform rand
    private final DataLoader.DataOptions dataOptions;


    private Integer percentage;
    private Random rand;
    private TempFileCreator fileCreator;

    @Getter
    private Path trainFile;

    @Getter
    private Path testFile;

    void extract() throws IOException {
        if (processed()) {
            return;
        }
        checkOptions();
        init();

        trainFile = fileCreator.create("train");
        testFile = fileCreator.create("test");

        Files.lines(dataPath)
                .filter(Predicate.not(String::isBlank))
                .forEach(this::processLine);
    }

    @SneakyThrows
    void cleanup() {
        fileCreator.cleanup();
    }

    @SneakyThrows
    private void processLine(String line) {
        var path = testFile;
        if (trainRandom()) {
            path = trainFile;
        }

        Files.writeString(path, line + System.lineSeparator(), StandardOpenOption.APPEND);
    }

    private boolean trainRandom() {
        return rand.nextInt(100) + 1 <= percentage;
    }

    private void checkOptions() {
        Integer percentage = dataOptions.getExtractTrainPercentage();
        if (percentage == null) {
            throw new IllegalArgumentException("Extract train percentage is null.");
        }
        if (percentage <= 0 || percentage >= 100) {
            throw new IllegalArgumentException(
                    "Extract train percentage is value is out of range (0, 100) = " + percentage);
        }
    }

    private void init() {
        percentage = dataOptions.getExtractTrainPercentage();
        if (dataOptions.getSeed() != null) {
            rand = new Random(dataOptions.getSeed());
        } else {
            rand = new Random();
        }
        fileCreator = new TempFileCreator();
    }

    private boolean processed() {
        return trainFile != null && testFile != null;
    }

}
