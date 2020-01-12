package pl.edu.pw.ddm.platform.algorithm.sample;

import com.sun.tools.javac.util.List;
import org.junit.Test;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

public class AlgorithmTest {

    @Test
    public void testAlgorithm() {
        Algorithm alg = new Algorithm();

        LocalModel l1 = alg.process(null, null);
        LocalModel l2 = alg.process(null, null);
        LocalModel l3 = alg.process(null, null);
        LocalModel l4 = alg.process(null, null);

        GlobalModel g = alg.process(List.of(l1, l2, l3, l4), null, null);

        System.out.println("Locals:");
        System.out.println(l1);
        System.out.println(l2);
        System.out.println(l3);
        System.out.println(l4);
        System.out.println("Global:");
        System.out.println(g);
    }

}