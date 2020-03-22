package pl.edu.pw.ddm.platform.runner.models;

import lombok.Value;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

@Value
public class StringModel implements LocalModel, GlobalModel {

    private String value;

}
