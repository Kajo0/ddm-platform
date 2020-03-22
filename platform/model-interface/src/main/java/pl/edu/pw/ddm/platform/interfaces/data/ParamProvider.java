package pl.edu.pw.ddm.platform.interfaces.data;

public interface ParamProvider {

    String provide(String name);

    double provideNumeric(String name);

}
