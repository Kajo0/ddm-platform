package pl.edu.pw.ddm.platform.algorithm.sample;

import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;

public class GModel implements GlobalModel {

    private final Integer sum;

    public GModel(Integer sum) {
        this.sum = sum;
    }

    public Integer getSum() {
        return sum;
    }

    @Override
    public String toString() {
        return "GModel{" + "sum=" + sum + '}';
    }

}
