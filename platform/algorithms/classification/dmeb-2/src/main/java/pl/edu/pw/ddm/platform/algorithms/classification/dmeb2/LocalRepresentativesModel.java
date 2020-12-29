package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.LabeledObservation;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.SVMModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

@Getter
@RequiredArgsConstructor
public class LocalRepresentativesModel implements LocalModel {

    public static final int CANNOT_LOCALHOST = -6;
    public static final int DUMMY_TARGET = -10;

    private final SVMModel svmModel;
    private final List<LabeledObservation> representativeList;

    public void clearRepresentativesButDummy() {
        representativeList.removeIf(lo -> lo.getTarget() != DUMMY_TARGET);
    }

    public static LabeledObservation dummyObservation() {
        try {
            return new LabeledObservation(InetAddress.getLocalHost().hashCode(), new int[]{}, DUMMY_TARGET);
        } catch (UnknownHostException e) {
            System.err.println("  [[FUTURE LOG]] Cannot InetAddress.getLocalHost(): " + e.getMessage());
            return new LabeledObservation(CANNOT_LOCALHOST, new int[]{}, DUMMY_TARGET);
        }
    }

}
