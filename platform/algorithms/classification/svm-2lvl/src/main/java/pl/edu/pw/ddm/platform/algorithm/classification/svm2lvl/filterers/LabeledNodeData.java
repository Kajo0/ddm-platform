package pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.filterers;

import lombok.Getter;
import pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.utils.LabeledObservation;

@lombok.Data
@Getter
public class LabeledNodeData implements Comparable<LabeledNodeData> {

    private final LabeledObservation labeled;

    @Override
    public int compareTo(LabeledNodeData o) {
        double[] attrs1 = labeled.getFeatures();
        double[] attrs2 = o.getLabeled()
                .getFeatures();
        return comp(attrs1, attrs2, 0);
    }

    private static int comp(double[] a, double[] b, int i) {
        if (i > a.length) {
            return 0;
        }

        if (a[i] == b[i]) {
            return comp(a, b, i + 1);
        } else {
            return Double.compare(a[i], b[i]);
        }
    }
}
