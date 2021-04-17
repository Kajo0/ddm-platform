package pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;

@Getter
@RequiredArgsConstructor
public class GlobalMinMaxModel implements GlobalModel {

    private final Data minValues;
    private final Data maxValues;

}
