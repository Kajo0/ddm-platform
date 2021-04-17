package pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.utils.LabeledObservation;
import pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.utils.SVMModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

@Getter
@RequiredArgsConstructor
public class LocalRepresentativesModel implements LocalModel {

    public static final int CANNOT_LOCALHOST = -6;
    public static final int DUMMY_TARGET = -10;

    private final SVMModel svmModel;
    private final Set<LabeledObservation> representatives;
    private final transient int trainingSize;
    private final transient int clusters;

    private transient Long labelClassSum;

    public void clearRepresentativesButDummy() {
        representatives.removeIf(lo -> lo.getTarget() != DUMMY_TARGET);
    }

    public static LabeledObservation dummyObservation() {
        try {
            return new LabeledObservation(InetAddress.getLocalHost().hashCode(), new int[]{}, DUMMY_TARGET);
        } catch (UnknownHostException e) {
            System.err.println("  [[FUTURE LOG]] Cannot InetAddress.getLocalHost(): " + e.getMessage());
            return new LabeledObservation(CANNOT_LOCALHOST, new int[]{}, DUMMY_TARGET);
        }
    }

    @Override
    public String customMetrics() {
        return representatives.size() + "/" + trainingSize + "%" + clusters;
    }

}
