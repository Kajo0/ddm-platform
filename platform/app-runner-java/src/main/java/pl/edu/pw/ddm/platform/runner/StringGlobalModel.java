package pl.edu.pw.ddm.platform.runner;

import lombok.Value;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;

@Value
class StringGlobalModel implements GlobalModel {

    private String value;

}
