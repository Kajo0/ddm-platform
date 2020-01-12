package pl.edu.pw.ddm.platform.algorithm.sample;

import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

public class LModel implements LocalModel {

    private final Integer num;

    public LModel(Integer num) {
        this.num = num;
    }

    public Integer getNum() {
        return num;
    }

    @Override
    public String toString() {
        return "LModel{" + "num=" + num + '}';
    }

}
