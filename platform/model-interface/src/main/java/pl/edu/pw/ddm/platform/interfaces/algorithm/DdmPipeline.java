package pl.edu.pw.ddm.platform.interfaces.algorithm;

import java.util.List;

import lombok.Value;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.Processor;

public interface DdmPipeline {

    List<ProcessingStage> getStages();

    @Value(staticConstructor = "of")
    class ProcessingStage {

        Stage stage;
        int stageIndex;
        Class<? extends Processor> processor;
    }

    enum Stage {
        LOCAL,
        LOCAL_REPEAT,
        LOCAL_UPDATE,
        GLOBAL,
        GLOBAL_UPDATE
    }

}
