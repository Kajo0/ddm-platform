package ddm.sample;

import java.util.Set;

import lombok.Value;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;

@Value
public class GModel implements GlobalModel {

    private Set<String> allLabels;

}
