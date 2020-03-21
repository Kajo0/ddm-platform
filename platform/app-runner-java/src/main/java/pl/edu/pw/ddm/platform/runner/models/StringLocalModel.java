package pl.edu.pw.ddm.platform.runner.models;

import lombok.Value;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

@Value
public class StringLocalModel implements LocalModel {

    private String value;

}
