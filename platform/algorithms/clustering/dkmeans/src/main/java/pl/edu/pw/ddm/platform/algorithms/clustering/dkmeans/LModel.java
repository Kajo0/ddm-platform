package pl.edu.pw.ddm.platform.algorithms.clustering.dkmeans;

import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

@Getter
@Builder
public class LModel implements LocalModel {

    @Singular("centroid")
    private final Set<double[]> centroids;

}
