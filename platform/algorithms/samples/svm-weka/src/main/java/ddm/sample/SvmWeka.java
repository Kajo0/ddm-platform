package ddm.sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import pl.edu.pw.ddm.platform.interfaces.algorithm.GlobalProcessor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.LocalProcessor;
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Utils;

public class SvmWeka implements LocalProcessor<LGModel, LGModel, Classifier>, GlobalProcessor<LGModel, LGModel> {

    @Override
    public LGModel processLocal(DataProvider dataProvider, ParamProvider paramProvider) {
        return new LGModel();
    }

    @SneakyThrows
    @Override
    public Classifier updateLocal(LGModel lModel, LGModel gModel, DataProvider dataProvider, ParamProvider paramProvider) {
        Collection<Data> training = dataProvider.training();
        List<String> labels = training.stream()
                .map(Data::getLabel)
                .distinct()
                .collect(Collectors.toList());

        Data sample = dataProvider.training()
                .iterator()
                .next();
        ArrayList<Attribute> attrs = new ArrayList<>();
        for (int i = 0; i < sample.getNumericAttributes().length; ++i) {
            attrs.add(new Attribute(String.valueOf(i)));
        }
        Attribute classAttr = new Attribute("target", labels);
        attrs.add(classAttr);

        Instances dataset = new Instances("trainingSet", attrs, training.size());
        dataset.setClass(classAttr);
        for (Data d : training) {
            DenseInstance di = new DenseInstance(attrs.size());
            for (int i = 0; i < d.getNumericAttributes().length; ++i) {
                di.setValue(i, d.getNumericAttribute(i));
            }
            di.setDataset(dataset);
            di.setClassValue(d.getLabel());
            dataset.add(di);
        }

        WekaClassifier smo = new WekaClassifier(labels);
        // default linear poly kernel with some parameters
        String[] options = Utils.splitOptions(paramProvider.provide("options", "-C 12.5 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -E 1.0 -C 250007\""));
        smo.setOptions(options);
        smo.buildClassifier(dataset);

        return smo;
    }

    @Override
    public LGModel processGlobal(Collection<LGModel> localModels, ParamProvider paramProvider) {
        return new LGModel();
    }

}
