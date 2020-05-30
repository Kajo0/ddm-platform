package pl.edu.pw.ddm.platform.algorithms.clustering.lct;

import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.Value;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;

@Getter
@Builder
public class GModel implements GlobalModel {

    @Singular("cluster")
    private final Set<GlobalCluster> clusters;

    @Value(staticConstructor = "of")
    public static class GlobalCluster {

        double[] centroid;
        String label;
    }

}
