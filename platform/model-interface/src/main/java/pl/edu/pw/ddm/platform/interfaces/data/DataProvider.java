package pl.edu.pw.ddm.platform.interfaces.data;

import java.util.Collection;

// TODO data lazy/partial loading
public interface DataProvider {

    Collection<Data> training();

    Collection<Data> test();

    Collection<Data> all();

}
