package ddm.samples;

import java.util.Arrays;

import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;

public class EqualityDistance implements DistanceFunction {

    @Override
    public String name() {
        return "equality";
    }

    @Override
    public double distance(Data first, Data second) {
        return first.equals(second) ? 0 : 1;
    }

    @Override
    public double distance(double[] first, double[] second) {
        return Arrays.equals(first, second) ? 0 : 1;
    }

    @Override
    public double distance(String[] first, String[] second) {
        return Arrays.equals(first, second) ? 0 : 1;
    }

}
