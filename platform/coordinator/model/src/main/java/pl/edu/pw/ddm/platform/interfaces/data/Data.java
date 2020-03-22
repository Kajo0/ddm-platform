package pl.edu.pw.ddm.platform.interfaces.data;

import java.io.Serializable;

public interface Data extends Serializable {

    String getId();

    String getLabel();

    String[] getAttributes();

    double[] getNumericAttributes();

    String getAttribute(int index);

    String getAttribute(String name);

    double getNumericAttribute(int index);

    double getNumericAttribute(String name);

}
