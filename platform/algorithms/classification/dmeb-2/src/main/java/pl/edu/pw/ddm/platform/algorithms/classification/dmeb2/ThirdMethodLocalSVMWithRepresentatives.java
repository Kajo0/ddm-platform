package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2;

import java.util.List;

import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.LabeledObservation;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb2.utils.SVMModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

public class ThirdMethodLocalSVMWithRepresentatives implements LocalModel {

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

}
