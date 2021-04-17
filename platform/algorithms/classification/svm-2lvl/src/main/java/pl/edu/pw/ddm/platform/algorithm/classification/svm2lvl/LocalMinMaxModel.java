package pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl;

import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

@Getter
@RequiredArgsConstructor
public class LocalMinMaxModel implements LocalModel {

    private final Set<Data> observations;

}
