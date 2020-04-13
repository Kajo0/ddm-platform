package pl.edu.pw.ddm.platform.interfaces.mining;

import java.io.Serializable;

public interface MiningMethod extends Serializable {

    String name();

    String type();

    interface MethodType {
        String CLUSTERING = "clustering";
        String CLASSIFICATION = "classification";
    }

}
