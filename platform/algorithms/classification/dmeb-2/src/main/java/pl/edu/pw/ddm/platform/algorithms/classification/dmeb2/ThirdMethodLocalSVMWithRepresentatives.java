package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.LabeledObservation;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.SVMModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

public class ThirdMethodLocalSVMWithRepresentatives implements LocalModel {

    public static final int CANNOT_LOCALHOST = -6;
    public static final int DUMMY_TARGET = -10;

    private SVMModel svmModel;
    private List<LabeledObservation> representativeList;

    public ThirdMethodLocalSVMWithRepresentatives(SVMModel svmModel, List<LabeledObservation> representativeList) {
        this.svmModel = svmModel;
        this.representativeList = representativeList;
    }

    public SVMModel getSvmModel() {
        return svmModel;
    }

    public List<LabeledObservation> getRepresentativeList() {
        return representativeList;
    }

    public void clearRepresentativesButDummy() {
        representativeList = representativeList.stream()
                .filter(lo -> lo.getTarget() == DUMMY_TARGET)
                .collect(Collectors.toList());
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
