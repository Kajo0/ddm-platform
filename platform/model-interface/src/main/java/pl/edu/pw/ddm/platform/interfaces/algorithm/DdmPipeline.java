package pl.edu.pw.ddm.platform.interfaces.algorithm;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.Processor;

public interface DdmPipeline {

    List<ProcessingStage> getStages();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor(staticName = "of")
    class ProcessingStage {

        private Stage stage;
        private int stageIndex;
        private String processorClassName;

        public Class<? extends Processor> processor() throws ClassNotFoundException {
            return (Class<? extends Processor>) Class.forName(processorClassName);
        }
    }

    enum Stage {
        LOCAL,
        LOCAL_REPEAT,
        LOCAL_UPDATE,
        GLOBAL,
        GLOBAL_UPDATE
    }

}
