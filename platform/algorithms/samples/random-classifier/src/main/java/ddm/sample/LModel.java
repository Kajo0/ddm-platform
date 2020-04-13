package ddm.sample;

import java.util.Set;

import lombok.Value;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

@Value
public class LModel implements LocalModel {

    private final Set<String> localLabels;

}
