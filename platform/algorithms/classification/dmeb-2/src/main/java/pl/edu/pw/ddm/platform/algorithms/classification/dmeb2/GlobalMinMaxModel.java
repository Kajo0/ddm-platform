package pl.edu.pw.ddm.platform.algorithms.classification.dmeb2;

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
