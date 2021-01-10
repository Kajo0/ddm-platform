package pl.edu.pw.ddm.platform.interfaces.mining;

import pl.edu.pw.ddm.platform.interfaces.model.BaseModel;

public interface MiningMethod extends BaseModel {

    String name();

    String type();

    interface MethodType {
        String CLUSTERING = "clustering";
        String CLASSIFICATION = "classification";
    }

}
