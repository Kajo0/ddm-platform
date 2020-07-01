package pl.edu.pw.ddm.platform.algorithms.classification.dmeb;

import java.util.List;

import pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils.LabeledObservation;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils.MEBModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

public class MEBBaseMethodLocalRepresentatives implements LocalModel {

    private List<LabeledObservation> representativeList;
    private MEBModel mebModel;

    public MEBBaseMethodLocalRepresentatives(List<LabeledObservation> representativeList, MEBModel mebModel) {
        this.representativeList = representativeList;
        this.mebModel = mebModel;
    }

    List<LabeledObservation> getRepresentativeList() {
        return representativeList;
    }

    public MEBModel getMebModel() {
        return mebModel;
    }

}
