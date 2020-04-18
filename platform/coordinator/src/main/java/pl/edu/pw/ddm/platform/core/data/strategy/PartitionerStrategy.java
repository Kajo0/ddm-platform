package pl.edu.pw.ddm.platform.core.data.strategy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface PartitionerStrategy {

    PartitionerStrategy UNIFORM = new UniformDataPartitioner();

    List<Path> partition(File dataFile, int workers, long samplesCount) throws IOException;

}
