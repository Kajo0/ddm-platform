package pl.edu.pw.ddm.platform.interfaces.data;

public interface ResultCollector {

    void collect(String id, String result);

    default void collect(String id, int result) {
        collect(id, String.valueOf(result));
    }

}
