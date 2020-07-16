package pl.edu.pw.ddm.platform.interfaces.data.strategy;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public interface PartitionFileCreator {

    void cleanup() throws IOException;

    Path create(String suffix) throws IOException;

    default Path create() throws IOException {
        return create(String.valueOf(System.currentTimeMillis()));
    }

    default List<Path> create(int numberOfFiles) throws IOException {
        long now = System.currentTimeMillis();
        List<Path> paths = new LinkedList<>();
        for (int i = 0; i < numberOfFiles; ++i) {
            Path file = create(now + "-" + i);
            paths.add(file);
        }
        return paths;
    }

}
