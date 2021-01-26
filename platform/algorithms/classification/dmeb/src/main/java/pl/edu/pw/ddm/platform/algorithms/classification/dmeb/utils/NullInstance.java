package pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils;

import weka.core.DenseInstance;

public class NullInstance extends DenseInstance {

    public static final NullInstance INSTANCE = new NullInstance();

    private NullInstance() {
        super(0);
    }

    @Override
    public String toString() {
        return "NullInstance";
    }

}
