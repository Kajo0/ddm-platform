package pl.edu.pw.ddm.platform.algorithms.clustering.lct;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import lombok.Getter;
import pl.edu.pw.ddm.platform.distfunc.EuclideanDistance;
import pl.edu.pw.ddm.platform.interfaces.algorithm.GlobalProcessor;
import pl.edu.pw.ddm.platform.interfaces.data.ParamProvider;

public class LightweightAggregator implements GlobalProcessor<LModel, GModel>,
        pl.edu.pw.ddm.platform.interfaces.algorithm.central.GlobalProcessor<LModel, GModel> {

    private final EuclideanDistance EUCLIDEAN_DISTANCE = new EuclideanDistance();

    private LinkedList<GlobalCluster> globalClusters;

    @Override
    public GModel processGlobal(Collection<LModel> localModels, ParamProvider paramProvider) {
        Double bParam = paramProvider.provideNumeric("b");
        merge(localModels);
        perturbate(bParam.intValue());

        AtomicInteger inc = new AtomicInteger(0);
        return globalClusters.stream()
                .map(c -> {
                    String label = String.valueOf(inc.getAndIncrement());
                    return GModel.GlobalCluster.of(c.getCentroid(), label);
                })
                .collect(Collectors.collectingAndThen(Collectors.toSet(), GModel::new));
    }

    private void merge(Collection<LModel> localModels) {
        globalClusters = localModels.stream()
                .map(LModel::getClusters)
                .flatMap(Collection::stream)
                .map(GlobalCluster::new)
                .collect(Collectors.toCollection(LinkedList::new));

        do {
            System.out.println("Start merging process for " + globalClusters.size() + " clusters.");
        } while (repeatMerge());
        System.out.println("Stop merging process at " + globalClusters.size() + " clusters.");
    }

    private boolean repeatMerge() {
        for (int i = 0; i < globalClusters.size(); ++i) {
            for (int j = 0; j < globalClusters.size(); ++j) {
                if (i == j) {
                    continue;
                }
                GlobalCluster first = globalClusters.get(i);
                GlobalCluster second = globalClusters.get(j);

                if (ClusterMerger.shouldMerge(first, second)) {
                    globalClusters.remove(second);
                    first.addCluster(second);
                    return true;
                }
            }
        }
        return false;
    }

    private void perturbate(Integer bParam) {
        for (int i = 0; i < globalClusters.size(); ++i) {
            GlobalCluster clusterI = globalClusters.get(i);
            List<LModel.LocalCluster> border = findBorder(clusterI, bParam);

            if (clusterI.isMultiAttributed()) {
                // FIXME unknown operation 'add multi attributed(i)'
//                border.add(clusterI.??);
            }

            for (int x = 0; x < border.size(); ++x) {
                LModel.LocalCluster clusterX = border.get(x);
                GlobalCluster clusterJ = findClosestBut(clusterX, clusterI);
                if (clusterJ == null) {
                    continue;
                }

                double var = clusterI.getVariance() + clusterJ.getVariance();
                double varnew = var(clusterI, clusterJ, clusterX);
                if (varnew < var) {
                    System.out.println("Variance decreased (" + var + "->" + varnew + ") after perturbation so moving cluster X from one to another.");
                    clusterI.removeCluster(clusterX);
                    // FIXME should remove I from global clusters when no sub cluster left there
//                    if (clusterI.isEmpty()) {
//                        globalClusters.remove(clusterI);
//                    }
                    clusterJ.addCluster(clusterX);
                }
            }
        }
    }

    private double var(GlobalCluster clusterI, GlobalCluster clusterJ, LModel.LocalCluster clusterX) {
        GlobalCluster clusterIWithoutX = new GlobalCluster(clusterI);
        clusterIWithoutX.removeCluster(clusterX);

        GlobalCluster clusterJWithX = new GlobalCluster(clusterJ);
        clusterJWithX.addCluster(clusterX);

        return clusterIWithoutX.getVariance() + clusterJWithX.getVariance();
    }

    private List<LModel.LocalCluster> findBorder(GlobalCluster clusterI, Integer bParam) {
        return clusterI.getSubClusters()
                .stream()
                .sorted(Comparator.comparingDouble((LModel.LocalCluster c) -> EUCLIDEAN_DISTANCE.distance(clusterI.getCentroid(), c.getCentroid())).reversed())
                .limit(bParam)
                .collect(Collectors.toList());
    }

    private GlobalCluster findClosestBut(LModel.LocalCluster clusterX, LModel.LocalCluster clusterI) {
        return globalClusters.stream()
                .filter(c -> c != clusterI)
                .min(Comparator.comparingDouble((LModel.LocalCluster c) -> EUCLIDEAN_DISTANCE.distance(clusterX.getCentroid(), c.getCentroid())))
                .orElse(null);
    }

    @Getter
    class GlobalCluster extends LModel.LocalCluster {

        private final Set<pl.edu.pw.ddm.platform.algorithms.clustering.lct.LModel.LocalCluster> subClusters = new HashSet<>();

        GlobalCluster(LModel.LocalCluster cluster) {
            subClusters.add(cluster);
            recalcStatistics();
        }

        GlobalCluster(GlobalCluster cluster) {
            subClusters.addAll(cluster.getSubClusters());
            recalcStatistics();
        }

        boolean isMultiAttributed() {
            return !subClusters.isEmpty();
        }

        void addCluster(LModel.LocalCluster localCluster) {
            subClusters.add(localCluster);
            recalcStatistics();
        }

        void addCluster(GlobalCluster globalCluster) {
            subClusters.addAll(globalCluster.getSubClusters());
            recalcStatistics();
        }

        boolean removeCluster(LModel.LocalCluster localCluster) {
            subClusters.remove(localCluster);
            if (!subClusters.isEmpty()) {
                recalcStatistics();
            }
            return subClusters.isEmpty();
        }

        private void recalcStatistics() {
            LModel.LocalCluster global = subClusters.stream()
                    .reduce(ClusterMerger::merge)
                    .orElseThrow(() -> new IllegalStateException("No sub clusters in global cluster"));

            setCentroid(global.getCentroid());
            setSize(global.getSize());
            setVariance(global.getVariance());
        }

    }

}
