package pl.edu.pw.ddm.platform.strategies.mostof;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class ScatteringLabelsPreparer {

    private final int workers;
    private final int labels;
    private final int additionalClassesNumber;
    /**
     * 0  - fill empty workers with all labels
     * >0 - fill next {given} number of labels on every empty node
     */
    private final int emptyWorkerFill;

    private void checkParams() {
        Preconditions.checkState(workers > 0, "Workers count cannot be less than 1");
        Preconditions.checkState(labels > 0, "Labels count cannot be less than 1");
        Preconditions.checkState(emptyWorkerFill >= 0, "Empty worker fill type must be greater equal than 0");
    }

    NodeToLabelScattering prepare() {
        checkParams();
        var scattering = new NodeToLabelScattering();

        for (int label = 0; label < labels; ++label) {
            int node = label % (workers);
            scattering.getFull()
                    .add(node, label);
        }

        log.info("workers={}, labels={}", workers, labels);
        if (workers == labels) {
            log.info("workers == labels -> no fill emtpy required");
        } else if (workers < labels) {
            log.info("workers < labels -> no fill emtpy required ");
        } else {
            log.info("workers > labels -> fill empty required");
        }

        if (emptyWorkerFill == 0) {
            fillEmptyNextSeparated(scattering, labels);
        } else if (emptyWorkerFill > 0) {
            fillEmptyNextSeparated(scattering, emptyWorkerFill);
        } else {
            throw new IllegalArgumentException("Not allowed emptyWorkerFill value = " + emptyWorkerFill);
        }

        if (additionalClassesNumber != 0) {
            prepareAdditional(scattering);
        }

        return scattering;
    }

    void prepareAdditional(NodeToLabelScattering scattering) {
        int classNumber = Math.abs(additionalClassesNumber);
        int additionalWorkers = Math.min(labels, workers);
        if (additionalClassesNumber < 0) {
            additionalWorkers = workers;
        }
        int currClass = 0;
        for (int worker = 0; worker < additionalWorkers; ++worker) {
            for (int i = 0; i < classNumber; ++i) {
                var additional = scattering.getAdditional();
                for (int label = 0; label < labels && additional.size(worker) < classNumber;
                        ++label, currClass = (currClass + 1) % labels) {
                    var alreadyHasFull = scattering.getFull()
                            .has(worker, currClass);
                    var alreadyAdded = additional.has(worker, currClass);
                    if (!alreadyHasFull && !alreadyAdded) {
                        additional.add(worker, currClass);
                    }
                }
            }
        }
    }

    private void fillEmptyNextSeparated(NodeToLabelScattering scattering, int amount) {
        amount = Math.min(labels, amount);
        int label = 0;
        for (int worker = labels; worker < workers; ++worker) {
            for (int i = 0; i < amount; ++i) {
                scattering.getEmpty()
                        .add(worker, label++);
                label %= labels;
            }
        }
    }

}
