package pl.edu.pw.ddm.platform.interfaces.algorithm.central;

import java.util.LinkedList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import pl.edu.pw.ddm.platform.interfaces.algorithm.DdmPipeline;
import pl.edu.pw.ddm.platform.interfaces.mining.MiningMethod;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CentralDdmPipeline implements DdmPipeline {

    @Getter
    private final List<ProcessingStage> stages = new LinkedList<>();

    private Global SATE_GLOBAL;
    private Local STATE_LOCAL;

    public static CentralDdmPipelineBuilder builder() {
        CentralDdmPipeline that = new CentralDdmPipeline();
        that.SATE_GLOBAL = that.new Global();
        that.STATE_LOCAL = that.new Local();
        return that.new CentralDdmPipelineBuilder();
    }

    private void addStage(Class<? extends Processor> processor, Stage stage) {
        stages.add(ProcessingStage.of(stage, stages.size(), processor));
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public class CentralDdmPipelineBuilder {

        public Global local(@NonNull Class<? extends LocalProcessor<? extends LocalModel>> localProcessorClass) {
            addStage(localProcessorClass, Stage.LOCAL);
            return SATE_GLOBAL;
        }

        public CentralDdmPipeline onlyLocal(@NonNull Class<? extends LocalProcessor<? extends LocalModel>> localProcessorClass) {
            addStage(localProcessorClass, Stage.LOCAL_UPDATE);
            return CentralDdmPipeline.this;
        }
    }

    public class Local {

        public Global repeatLocal(@NonNull Class<? extends LocalRepeater<? extends LocalModel, ? extends LocalModel, ? extends GlobalModel>> localRepeaterClass) {
            addStage(localRepeaterClass, Stage.LOCAL_REPEAT);
            return SATE_GLOBAL;
        }

        public CentralDdmPipeline lastLocal(@NonNull Class<? extends LocalUpdater<? extends LocalModel, ? extends GlobalModel, ? extends MiningMethod>> localUpdaterClass) {
            addStage(localUpdaterClass, Stage.LOCAL_UPDATE);
            return CentralDdmPipeline.this;
        }
    }

    public class Global {

        public Local global(@NonNull Class<? extends GlobalProcessor<? extends LocalModel, ? extends GlobalModel>> globalProcessorClass) {
            addStage(globalProcessorClass, Stage.GLOBAL);
            return STATE_LOCAL;
        }

        public CentralDdmPipeline lastGlobal(@NonNull Class<? extends GlobalProcessor<? extends LocalModel, ? extends GlobalModel>> globalProcessorClass) {
            addStage(globalProcessorClass, Stage.GLOBAL_UPDATE);
            return CentralDdmPipeline.this;
        }
    }

}
