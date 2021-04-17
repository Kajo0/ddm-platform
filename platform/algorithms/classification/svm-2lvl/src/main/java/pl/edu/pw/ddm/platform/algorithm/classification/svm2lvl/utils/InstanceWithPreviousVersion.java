package pl.edu.pw.ddm.platform.algorithm.classification.svm2lvl.utils;

import weka.core.DenseInstance;
import weka.core.Instance;

public class InstanceWithPreviousVersion extends DenseInstance {

    private Instance before;

    InstanceWithPreviousVersion(Instance after, Instance before) {
        super(after);
        this.before = before;
    }

    Instance getBefore() {
        return before;
    }

    @Override
    public Object copy() {
        InstanceWithPreviousVersion result = new InstanceWithPreviousVersion(this, before);
        result.m_Dataset = m_Dataset;
        return result;
    }
}
