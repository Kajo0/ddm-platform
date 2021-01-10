package pl.edu.pw.ddm.platform.algorithms.classification.dmeb;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils.LabeledObservation;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;

@Getter
@RequiredArgsConstructor
public class MEBBaseMethodChosenRepresentatives implements GlobalModel {

    private final List<LabeledObservation> representativeList;

    @Override
    public String customMetrics() {
        return String.valueOf(representativeList.size());
    }

}
