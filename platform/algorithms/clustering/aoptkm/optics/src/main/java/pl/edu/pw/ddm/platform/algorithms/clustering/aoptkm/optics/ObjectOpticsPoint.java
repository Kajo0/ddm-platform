package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.optics;

import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.point.ObjectPoint;

public class ObjectOpticsPoint extends ObjectPoint {

    public boolean processed = false;
    public Double reachabilityDistance = null;
    public Double coreDistance = null;

    public ObjectOpticsPoint(Object[] values, Integer index) {
        super(values, index);
    }

    public ObjectOpticsPoint(Object[] values) {
        super(values);
    }

    @Override
    public String toString() {
        return String.format("%s [proc=%5b,cd=%10.2f,rd=%10.2f]", super.toString(), processed, coreDistance,
                reachabilityDistance);
    }

}
