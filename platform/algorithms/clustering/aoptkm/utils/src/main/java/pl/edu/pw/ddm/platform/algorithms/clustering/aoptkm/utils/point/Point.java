package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.point;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.distance.DistanceFunction;

/**
 * Point model representation wrapper, used to collect calculated cluster id for point
 *
 * @param <T> Representation of point - usually double[] for vector space model
 */
@EqualsAndHashCode
public abstract class Point<T> implements Serializable {

    /**
     * When point isn't clustered yet
     */
    public static final int UNCLASSIFIED = -1;
    /**
     * When point is noise
     */
    public static final int NOISE = 0;

    /**
     * Cluster Id
     */
    public int clusterId = UNCLASSIFIED;
    public Integer originalLabel = null;
    /**
     * Point index
     */
    public Integer index = null;
    /**
     * Point points
     */
    public T values;

    /**
     * C-tor
     *
     * @param values Initial values
     * @param index  Initial index
     */
    public Point(T values, Integer index) {
        this.values = values;
        this.index = index;
    }

    /**
     * C-tor
     *
     * @param values Initial values
     */
    public Point(T values) {
        this.values = values;
    }

    /**
     * Calculates distance between points using given {code}distanceFunc{code}
     *
     * @param another      Another point to calc distance with
     * @param distanceFunc distance function to usee
     * @return calculated distance
     */
    public double distance(Point<T> another, DistanceFunction distanceFunc) {
        return distanceFunc.distance(this.values, another.values);
    }

    public static int nextId(int currentId) {
        return currentId + 1;
    }

    @Override
    public String toString() {
        return String.format("[index=%3d,cl=%2d]", index, clusterId);
    }

}
