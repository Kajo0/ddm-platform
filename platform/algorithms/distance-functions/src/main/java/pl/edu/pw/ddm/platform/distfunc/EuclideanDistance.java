package pl.edu.pw.ddm.platform.distfunc;

import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;

public class EuclideanDistance implements DistanceFunction {

    @Override
    public String name() {
        return PredefinedNames.EUCLIDEAN;
    }

    @Override
    public double distance(Data first, Data second) {
        // TODO improve for non numeric
        return distance(first.getNumericAttributes(), second.getNumericAttributes());
    }

    @Override
    public double distance(double[] first, double[] second) {
        if (first.length != second.length) {
            throw new IllegalArgumentException();
        }

        double ret = 0;

        for (int i = 0; i < first.length; ++i) {
            ret += Math.pow(first[i] - second[i], 2.0);
        }

        return Math.sqrt(ret);
    }

    @Override
    public double distance(String[] first, String[] second) {
        if (first.length != second.length) {
            throw new IllegalArgumentException();
        }

        double[] f = new double[first.length];
        double[] s = new double[second.length];
        for (int i = 0; i < first.length; ++i) {
            f[i] = parseDouble(first[i]);
            s[i] = parseDouble(second[i]);
        }

        return distance(f, s);
    }

    // TODO improve for non numeric
    private double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return -0.001;
        }
    }

}
