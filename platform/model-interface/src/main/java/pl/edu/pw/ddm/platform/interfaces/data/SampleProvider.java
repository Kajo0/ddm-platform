package pl.edu.pw.ddm.platform.interfaces.data;

import java.util.Collection;
import java.util.Iterator;

public interface SampleProvider extends Iterator<SampleData> {

    Collection<SampleData> all();

}
