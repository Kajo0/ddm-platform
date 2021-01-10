package pl.edu.pw.ddm.platform.interfaces.model;

import java.io.Serializable;

public interface BaseModel extends Serializable {

    /**
     * Custom metrics data used for algorithm evaluation purposes
     *
     * @return stringified custom data
     */
    default String customMetrics() {
        return null;
    }

}
