package pl.edu.pw.ddm.platform.runner;

import lombok.Value;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

@Value
class StringLocalModel implements LocalModel {

    private String value;

}
