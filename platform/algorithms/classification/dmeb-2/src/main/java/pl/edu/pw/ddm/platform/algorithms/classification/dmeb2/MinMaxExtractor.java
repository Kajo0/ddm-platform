package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import lombok.RequiredArgsConstructor;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.UnlabeledObservation;
import pl.edu.pw.ddm.platform.interfaces.data.Data;

@RequiredArgsConstructor
class MinMaxExtractor {

    private final Collection<Data> data;

    List<Data> search() {
        int length = data.stream()
                .map(Data::getAttributes)
                .map(a -> a.length)
                .findAny()
                .orElse(0);
        double[] mins = new double[length];
        double[] maxs = new double[length];
        for (int i = 0; i < length; ++i) {
            mins[i] = Double.MAX_VALUE;
            maxs[i] = Double.MIN_VALUE;
        }
        data.stream()
                .map(Data::getNumericAttributes)
                .forEach(features -> {
                    for (int i = 0; i < length; ++i) {
                        if (mins[i] > features[i]) {
                            mins[i] = features[i];
                        }
                        if (maxs[i] < features[i]) {
                            maxs[i] = features[i];
                        }
                    }
                });

        return Arrays.asList(new UnlabeledObservation(mins), new UnlabeledObservation(maxs));
    }

}
