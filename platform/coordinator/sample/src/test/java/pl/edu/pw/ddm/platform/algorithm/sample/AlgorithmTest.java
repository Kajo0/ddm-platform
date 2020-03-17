package pl.edu.pw.ddm.platform.algorithm.sample;

import java.util.List;

import org.junit.Test;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ResultCollector;
import pl.edu.pw.ddm.platform.interfaces.data.SampleProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.Classifier;

public class AlgorithmTest {

    @Test
    public void testAlgorithm() {
        DataProvider dp = null;
        ParamProvider pp = null;
        SampleProvider sp = null;
        ResultCollector rp = null;

        Algorithm alg = new Algorithm();

        // RDD map here
        LModel l1 = alg.processLocal(dp, pp);
        LModel l2 = alg.processLocal(dp, pp);
        LModel l3 = alg.processLocal(dp, pp);
        LModel l4 = alg.processLocal(dp, pp);

        // Central process
        GModel g = alg.processGlobal(List.of(l1, l2, l3, l4), pp);

        // Debug info
        System.out.println("Locals:");
        System.out.println(l1);
        System.out.println(l2);
        System.out.println(l3);
        System.out.println(l4);
        System.out.println("Global:");
        System.out.println(g);

        // RDD map update
        Classifier m1 = alg.updateLocal(l1, g, dp, pp);
        Classifier m2 = alg.updateLocal(l2, g, dp, pp);
        Classifier m3 = alg.updateLocal(l3, g, dp, pp);
        Classifier m4 = alg.updateLocal(l4, g, dp, pp);

        // Perform mining
        m1.classify(sp, rp);
        m2.classify(sp, rp);
        m3.classify(sp, rp);
        m4.classify(sp, rp);
    }

}
