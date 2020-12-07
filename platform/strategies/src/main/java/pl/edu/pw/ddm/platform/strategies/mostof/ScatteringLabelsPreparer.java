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

    private void checkParams() {
        Preconditions.checkState(workers > 0, "Workers count cannot be less than 1");
        Preconditions.checkState(labels > 0, "Labels count cannot be less than 1");
        Preconditions.checkState(additionalClassesNumber >= 0, "Additional classes number cannot be negative");
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

        for (int worker = labels; worker < workers; ++worker) {
            for (int label = 0; label < labels; ++label) {
                scattering.getEmpty()
                        .add(worker, label);
            }
        }

        if (additionalClassesNumber > 0) {
            prepareAdditional(scattering);
        }

        return scattering;
    }

    void prepareAdditional(NodeToLabelScattering scattering) {
        int currClass = 0;
        for (int worker = 0; worker < Math.min(labels, workers); ++worker) {
            for (int i = 0; i < additionalClassesNumber; ++i) {
                var additional = scattering.getAdditional();
                for (int label = 0; label < labels && additional.size(worker) < additionalClassesNumber;
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

}
