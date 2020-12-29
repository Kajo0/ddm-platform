package pl.edu.pw.ddm.platform.algorithms.classification.dmeb;

import java.util.Set;

import pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils.LabeledObservation;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils.MEBModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

public class MEBBaseMethodLocalRepresentatives implements LocalModel {

    private Set<LabeledObservation> representativeList;
    private MEBModel mebModel;

    public MEBBaseMethodLocalRepresentatives(Set<LabeledObservation> representativeList, MEBModel mebModel) {
        this.representativeList = representativeList;
        this.mebModel = mebModel;
    }

    Set<LabeledObservation> getRepresentativeList() {
        return representativeList;
    }

    public MEBModel getMebModel() {
        return mebModel;
    }

}
