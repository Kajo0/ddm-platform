package pl.edu.pw.ddm.platform.algorithms.clustering.dkmeans;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
public class LModel implements LocalModel {

    @Singular("centroid")
    private final Set<double[]> centroids;

    @Override
    public String toString() {
        return centroids.stream()
                .map(Arrays::toString)
                .collect(Collectors.joining(";"));
    }
}
