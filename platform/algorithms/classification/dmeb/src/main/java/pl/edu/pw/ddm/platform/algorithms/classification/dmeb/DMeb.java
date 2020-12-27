package pl.edu.pw.ddm.platform.algorithms.classification.dmeb;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils.LabeledObservation;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils.MEBClustering;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils.MEBModel;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils.SVMModel;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils.Utils;
import pl.edu.pw.ddm.platform.algorithms.classification.dmeb.utils.WekaSVMClassification;
import pl.edu.pw.ddm.platform.interfaces.algorithm.AlgorithmConfig;
import pl.edu.pw.ddm.platform.interfaces.algorithm.DdmPipeline;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.CentralDdmPipeline;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.GlobalProcessor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.GlobalUpdater;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.LocalProcessor;
import pl.edu.pw.ddm.platform.interfaces.algorithm.central.LocalRepeater;
import pl.edu.pw.ddm.platform.interfaces.data.Data;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;
import pl.edu.pw.ddm.platform.interfaces.data.ResultCollector;
import pl.edu.pw.ddm.platform.interfaces.data.SampleProvider;
import pl.edu.pw.ddm.platform.interfaces.mining.Classifier;

public class DMeb implements LocalProcessor<MEBBaseMethodLocalRepresentatives>,
        GlobalProcessor<MEBBaseMethodLocalRepresentatives, MEBBaseMethodChosenRepresentatives>,
        LocalRepeater<MEBBaseMethodLocalRepresentatives, MEBBaseMethodDeClustered, MEBBaseMethodChosenRepresentatives>,
        GlobalUpdater<MEBBaseMethodDeClustered, DMeb>,
        AlgorithmConfig,
        Classifier {

    private SVMModel globalSVM;

    @Override
    public MEBBaseMethodLocalRepresentatives processLocal(DataProvider dataProvider, ParamProvider paramProvider) {
        printParams(paramProvider);

        List<LabeledObservation> labeledObservations = toLabeledObservation(dataProvider.training());
        // FIXME unused partitionId?
        int partitionId = 0;
        Double mebClusters = paramProvider.provideNumeric("meb_clusters", 32d);
        if (mebClusters <= 0) {
            mebClusters = Math.max(2, Math.ceil(Math.pow(Math.log(dataProvider.training().size()), 2)));
            System.out.println("  [[FUTURE LOG]] MEB clusters calculated=" + mebClusters);
        }
        MEBModel mebModel = new MEBClustering(mebClusters.intValue()).perform(labeledObservations, partitionId);
        List<LabeledObservation> representativeList = mebModel.getClusterList().stream()
                .flatMap(cluster -> {
                    if (Utils.moreThanOneClass(cluster.getClusterElementList())) {
                        return cluster.getClusterElementList().stream();
                    } else {
                        return Stream.of(cluster.squashToCentroid());
                    }
                })
                .collect(Collectors.toList());

        System.out.println("  [[FUTURE LOG]] processLocal: representativeList=" + representativeList.size()
                + ", labeledObservations=" + labeledObservations.size());
        return new MEBBaseMethodLocalRepresentatives(representativeList, mebModel);
    }

    private List<LabeledObservation> toLabeledObservation(Collection<Data> training) {
        return training.stream()
                .map(d -> new LabeledObservation(Integer.parseInt(d.getId()), d.getNumericAttributes(), (int) Double.parseDouble(d.getLabel()))) // FIXME int label/index
                .collect(Collectors.toList());
    }

    @Override
    public MEBBaseMethodChosenRepresentatives processGlobal(Collection<MEBBaseMethodLocalRepresentatives> localModels, ParamProvider paramProvider) {
        List<LabeledObservation> trainingSet = localModels.stream()
                .flatMap(localModel -> localModel.getRepresentativeList().stream())
                .collect(Collectors.toList());
        String kernel = paramProvider.provide("kernel");
        SVMModel svmModel = new WekaSVMClassification(kernel).train(trainingSet);
        List<LabeledObservation> observations = localModels.stream().flatMap(m ->
                m.getMebModel().getClusterList().stream()
                        // FIXME KJ
                        // no SVs = linear model..
                        .filter(cluster -> svmModel.getSVs().isEmpty() || cluster.containsAny(svmModel.getSVs()))
                        .flatMap(cluster -> cluster.getClusterElementList().stream()))
                .collect(Collectors.toList());

        System.out.println("  [[FUTURE LOG]] processGlobal: observations=" + observations.size()
                + ", svs=" + svmModel.getSVs().size());
        return new MEBBaseMethodChosenRepresentatives(observations);
    }

    @Override
    public MEBBaseMethodDeClustered repeatLocal(MEBBaseMethodChosenRepresentatives gModel, MEBBaseMethodLocalRepresentatives lModel, DataProvider dataProvider, ParamProvider paramProvider) {
        MEBModel mebModel = lModel.getMebModel();
        if (mebModel == null) {
            System.out.println("mebModel is null");
        } else if (mebModel.getClusterList() == null) {
            System.out.println("mebModel cluster list is null");
        }
        List<LabeledObservation> observations = mebModel.getClusterList().stream()
                // FIXME KJ
//		List<LabeledObservation> observations = Optional.ofNullable(mebModel)
//				.map(MEBModel::getClusterList)
//				.orElseGet(Collections::emptyList)
//				.stream()
                .filter(cluster -> cluster.containsAny(gModel.getRepresentativeList()))
                .flatMap(cluster -> cluster.getClusterElementList().stream())
                .collect(Collectors.toList());
//        this.representativeList = observations;

        System.out.println("  [[FUTURE LOG]] repeatLocal: observations=" + observations.size());
        return new MEBBaseMethodDeClustered(observations);
    }

    @Override
    public DMeb updateGlobal(Collection<MEBBaseMethodDeClustered> localModels, ParamProvider paramProvider) {
        List<LabeledObservation> trainingSet = localModels.stream()
                .flatMap(localModel -> localModel.getRepresentativeList().stream())
                .collect(Collectors.toList());
        String kernel = paramProvider.provide("kernel");
        globalSVM = new WekaSVMClassification(kernel).train(trainingSet);

        System.out.println("  [[FUTURE LOG]] updateGlobal: svs=" + globalSVM.getSVs().size()
                + ", trainingSet=" + trainingSet.size());
        return this;
    }

    @Override
    public void classify(SampleProvider sampleProvider, ParamProvider paramProvider, ResultCollector resultCollector) {
        sampleProvider.forEachRemaining(sample -> resultCollector.collect(sample.getId(), String.valueOf(globalSVM.classify(sample.getNumericAttributes()))));
    }

    @Override
    public String name() {
        return "D-MEB";
    }

    @Override
    public DdmPipeline pipeline() {
        return CentralDdmPipeline.builder()
                .local(DMeb.class)
                .global(DMeb.class)
                .repeatLocal(DMeb.class)
                .lastGlobal(DMeb.class);
    }

    private void printParams(ParamProvider paramProvider) {
        System.out.println("---------------------------------");
        System.out.println("-     PARAMS                    -");
        System.out.println("---------------------------------");
        paramProvider.allParams()
                .forEach((k, v) -> System.out.println("  " + k + "=" + v));
        System.out.println("---------------------------------");
    }

}
