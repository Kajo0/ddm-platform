package pl.edu.pw.ddm.platform.interfaces.data;

import java.io.Serializable;

public interface Data extends Serializable {

    String getId();

    String getLabel();

    String[] getAttributes();

    double[] getNumericAttributes();

    String getAttribute(int col);

    String getAttribute(String name);

    double getNumericAttribute(int col);

    double getNumericAttribute(String name);

}
