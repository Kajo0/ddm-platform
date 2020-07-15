package pl.edu.pw.ddm.platform.runner.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import pl.edu.pw.ddm.platform.interfaces.mining.MiningMethod;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;

@Data
@RequiredArgsConstructor(staticName = "of")
public class GlobalMethodModelWrapper implements GlobalModel {

    private final MiningMethod miningMethod;

}
