package pl.edu.pw.ddm.platform.core.data.strategy;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.edu.pw.ddm.platform.core.data.dto.DataDescDto;

public interface PartitionerStrategy {

    Map<String, PartitionerStrategy> STRATEGIES = Map.of(
            Strategies.UNIFORM.code, new UniformDataPartitioner(),
            Strategies.SEPARATE_BY_LABELS.code, new SeparateByLabelsPartitioner()
    );

    List<Path> partition(DataDescDto dataDesc, int workers, long samplesCount, String params) throws IOException;

    @Getter
    @AllArgsConstructor
    enum Strategies {
        UNIFORM("uniform"),
        SEPARATE_BY_LABELS("separate-labels"),
        DEFAULT(UNIFORM.code);

        private final String code;
    }

}
