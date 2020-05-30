package pl.edu.pw.ddm.platform.algorithms.clustering.lct;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

@Getter
@Builder
public class LModel implements LocalModel {

    @Singular("cluster")
    private final Set<LocalCluster> clusters;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor(staticName = "of")
    public static class LocalCluster {

        private double[] centroid;
        private int size;
        private double variance;
    }

}
