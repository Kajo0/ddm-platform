package pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.filterers;

import lombok.RequiredArgsConstructor;
import pl.edu.pw.ddm.platform.interfaces.data.Data;

@lombok.Data
@RequiredArgsConstructor
public class NodeData implements Comparable<NodeData> {

    private final Data data;

    @Override
    public int compareTo(NodeData o) {
        double[] attrs1 = data.getNumericAttributes();
        double[] attrs2 = o.getData()
                .getNumericAttributes();
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
