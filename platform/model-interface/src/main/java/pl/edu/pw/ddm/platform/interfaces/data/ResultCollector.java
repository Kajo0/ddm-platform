package pl.edu.pw.ddm.platform.interfaces.data;

public interface ResultCollector {

    void collect(String id, String result);

    void collect(String id, double result);

}
