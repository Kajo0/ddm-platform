package pl.edu.pw.ddm.platform.runner.models;

import lombok.Value;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;

@Value
public class StringGlobalModel implements GlobalModel {

    private String value;

}
