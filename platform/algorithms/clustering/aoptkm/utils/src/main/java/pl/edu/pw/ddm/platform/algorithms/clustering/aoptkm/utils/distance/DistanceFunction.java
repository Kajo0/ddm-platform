package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.distance;

import java.util.List;

/**
 * Function used to calculate distance between two 'points'
 *
 * @param <T> Type of 'points'
 */
public interface DistanceFunction<T> {

    /**
     * Calculate distance between given objects
     *
     * @param first  First object
     * @param second Second object
     * @return Calculated distance
     */
    double distance(T first, T second);

    T meanMerge(List<T> list);

}
