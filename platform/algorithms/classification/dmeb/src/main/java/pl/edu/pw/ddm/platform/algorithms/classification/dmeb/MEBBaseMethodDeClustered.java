package pl.edu.pw.ddm.platform.algorithms.classification.dmeb;

import java.util.List;

import pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils.LabeledObservation;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

public class MEBBaseMethodDeClustered implements LocalModel {

    private List<LabeledObservation> representativeList;

    public MEBBaseMethodDeClustered(List<LabeledObservation> representativeList) {
        this.representativeList = representativeList;
    }

    public List<LabeledObservation> getRepresentativeList() {
        return representativeList;
    }

}
