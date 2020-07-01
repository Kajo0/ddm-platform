package pl.edu.pw.ddm.platform.algorithms.classification.dmeb;

import java.util.List;

import pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils.LabeledObservation;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;

public class MEBBaseMethodChosenRepresentatives implements GlobalModel {

    private List<LabeledObservation> representativeList;

    public MEBBaseMethodChosenRepresentatives(List<LabeledObservation> representativeList) {
        this.representativeList = representativeList;
    }

    public List<LabeledObservation> getRepresentativeList() {
        return representativeList;
    }

}
