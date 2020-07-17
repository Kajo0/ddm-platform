package pl.edu.pw.ddm.platform.core.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionFileCreator;

class TempFileCreator implements PartitionFileCreator {

    private final List<Path> files = new LinkedList<>();

    @Override
    public void cleanup() throws IOException {
        for (Path file : files) {
            Files.deleteIfExists(file);
        }
        files.clear();
    }

    @Override
    public Path create(String suffix) throws IOException {
        Path file = Files.createTempFile("splitter", suffix);
        files.add(file);
        return file;
    }

}
