package pl.edu.pw.ddm.platform.interfaces.data;

import java.util.Collection;

public interface DataProvider {

    Collection<Data> trainig();

    Collection<Data> test();

    Collection<Data> all();

}
