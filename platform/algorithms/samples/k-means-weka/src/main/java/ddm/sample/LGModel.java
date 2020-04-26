package ddm.sample;

import lombok.Value;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

@Value
public class LGModel implements LocalModel, GlobalModel {
    // intentionally empty
}
