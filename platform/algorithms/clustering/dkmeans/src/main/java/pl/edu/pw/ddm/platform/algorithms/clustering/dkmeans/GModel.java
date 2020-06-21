package pl.edu.pw.ddm.platform.algorithms.clustering.dkmeans;

import java.io.Serializable;
import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.Value;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;

@Getter
@Builder
public class GModel implements GlobalModel {

    @Singular("centroid")
    private final Set<GlobalCluster> clusters;

    @Value(staticConstructor = "of")
    public static class GlobalCluster implements Serializable {

        double[] centroid;
        Integer label;
    }

}
