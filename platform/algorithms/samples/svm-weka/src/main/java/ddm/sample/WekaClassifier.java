package ddm.sample;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import pl.edu.pw.ddm.platform.interfaces.algorithm.AlgorithmConfig;
import pl.edu.pw.ddm.platform.interfaces.algorithm.DdmPipeline;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.CentralDdmPipeline;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ResultCollector;
import pl.edu.pw.ddm.platform.interfaces.data.SampleData;
import pl.edu.pw.ddm.platform.interfaces.data.SampleProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.Classifier;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class WekaClassifier extends SMO implements Classifier, AlgorithmConfig {

    private List<String> labels;
    private Map<Integer, String> labelMap = new HashMap<>();

    public WekaClassifier(List<String> labels) {
        this.labels = labels;
        for (int i = 0; i < labels.size(); ++i) {
            labelMap.put(i, labels.get(i));
        }
    }

    @SneakyThrows
    @Override
    public void classify(SampleProvider sampleProvider, ParamProvider paramProvider, ResultCollector resultCollector) {
        SampleData s = sampleProvider.all().iterator().next();
        ArrayList<Attribute> attrs = new ArrayList<>();
        for (int i = 0; i < s.getNumericAttributes().length; ++i) {
            attrs.add(new Attribute(String.valueOf(i)));
        }
        Attribute classAttr = new Attribute("target", labels, null);
        attrs.add(classAttr);

        Instances dataset = new Instances("evaluationSet", attrs, 1);
        dataset.setClass(classAttr);

        int[] i = new int[]{0};
        while (sampleProvider.hasNext()) {
            Instance instance = new DenseInstance(1.0, sampleProvider.next().getNumericAttributes());
            instance.setDataset(dataset);
            resultCollector.collect(String.valueOf(i[0]++), labelMap.get((int) this.classifyInstance(instance)));
        }
    }

    @Override
    public String name() {
        return "WEKA SVM";
    }

    @Override
    public DdmPipeline pipeline() {
        return CentralDdmPipeline.builder()
                .onlyLocal(SvmWeka.class);
    }

    @Override
    public boolean onlySingleNode() {
        return true;
    }

}
