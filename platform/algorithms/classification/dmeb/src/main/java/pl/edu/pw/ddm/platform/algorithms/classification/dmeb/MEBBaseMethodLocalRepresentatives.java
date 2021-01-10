package pl.edu.pw.ddm.platform.algorithms.classification.dmeb;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils.LabeledObservation;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils.MEBCluster;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils.MEBModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

@RequiredArgsConstructor
public class MEBBaseMethodLocalRepresentatives implements LocalModel {

    private final MEBModel mebModel;
    private final int trainingSize;

    public Set<LabeledObservation> getRepresentativeList() {
        return mebModel.getClusterList()
                .stream()
                .map(MEBCluster::getClusterElementList)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public MEBModel getMebModel() {
        return mebModel;
    }

    @Override
    public String customMetrics() {
        return getRepresentativeList().size() + "/" + trainingSize + "%" + mebModel.getClusterList().size();
    }
}
