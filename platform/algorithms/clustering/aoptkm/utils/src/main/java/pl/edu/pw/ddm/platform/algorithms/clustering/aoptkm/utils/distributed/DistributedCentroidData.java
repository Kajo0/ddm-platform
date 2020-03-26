package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.distributed;

import java.io.Serializable;
import java.util.Arrays;

public class DistributedCentroidData implements Serializable {

    private double value;
    private int numberOfPoints;
    private Object[] centroid;
    private double sum;
    private double min;
    private double max;
    private double mean;
    private double squareSum;
    private double standardDeviation;

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Integer getN() {
        return numberOfPoints;
    }

    public void setN(Integer numberOfPoints) {
        this.numberOfPoints = numberOfPoints;
    }

    public Object[] getCentroid() {
        return centroid;
    }

    public void setCentroid(Object[] centroid) {
        this.centroid = centroid;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public Double getS() {
        return sum;
    }

    public void setS(Double sum) {
        this.sum = sum;
    }

    public Double getM() {
        return mean;
    }

    public void setM(Double mean) {
        this.mean = mean;
    }

    public Double getSS() {
        return squareSum;
    }

    public void setSS(Double squareSum) {
        this.squareSum = squareSum;
    }

    public Double getSD() {
        return standardDeviation;
    }

    public void setSD(Double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    @Override
    public String toString() {
        return "DistributedCentroidData{" +
                "value=" + value +
                ", numberOfPoints=" + numberOfPoints +
                ", centroid=" + Arrays.toString(centroid) +
                ", sum=" + sum +
                ", min=" + min +
                ", max=" + max +
                ", mean=" + mean +
                ", squareSum=" + squareSum +
                ", standardDeviation=" + standardDeviation +
                '}';
    }

}
