package pl.edu.pw.ddm.platform.strategies.mostof;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class ScatteringAmountsPreparer {

    private final Map<String, Long> labelCount;
    private final NodeToLabelScattering labelScattering;
    private final double fillEmptyButPercent;
    private final double additionalClassesPercent;

    private double maxEmptyPercent;
    private double maxAdditionalPercent;
    private final Map<Integer, String> labelMapping = new HashMap<>();

    private void checkParams() {
        maxEmptyPercent = 1 - fillEmptyButPercent;
        maxAdditionalPercent = fillEmptyButPercent / 2;

        Preconditions.checkState(fillEmptyButPercent >= 0 && fillEmptyButPercent < 1,
                "fillEmptyButPercent must be in range <0, 1)");
        Preconditions.checkState(additionalClassesPercent >= 0 && additionalClassesPercent < maxEmptyPercent,
                "additionalClassesPercent must be in range <0, 1 - fillEmptyButPercent)");

        var genLabels = labelScattering.labels();
        var realLabels = labelCount.keySet();

        Preconditions.checkState(realLabels.size() == genLabels.size(),
                "Labels count amount '%s' differs than scattering labels '%s'.", realLabels.size(), genLabels.size());

        var genIter = genLabels.iterator();
        var realIter = realLabels.iterator();
        while (genIter.hasNext() && realIter.hasNext()) {
            labelMapping.put(genIter.next(), realIter.next());
        }
        log.info("Label mapping is done: '{}'.", labelMapping);
    }

    NodeToLabelAmountScattering prepare() {
        checkParams();
        var scattering = new NodeToLabelAmountScattering();

        // full labels
        var fullWorkers = labelScattering.getFull();
        fullWorkers.nodes()
                .forEach(node -> fullWorkers.labels(node)
                        .forEach(label -> scattering.put(node, mapLabel(label), labelCount.get(mapLabel(label))
                                .intValue())));
        // empty workers
        var emptyWorkers = labelScattering.getEmpty();
        fullWorkers.nodes()
                .forEach(fullNode -> fullWorkers.labels(fullNode)
                        .stream()
                        .filter(fullLabel -> emptyWorkers.countLabelInNodes(fullLabel) > 0)
                        .forEach(fullLabel -> {
                            double perClass = maxEmptyPercent / emptyWorkers.countLabelInNodes(fullLabel);
                            int amount = (int) Math.ceil(scattering.get(fullNode, mapLabel(fullLabel)) * perClass);

                            emptyWorkers.nodes()
                                    .stream()
                                    .filter(emptyNode -> emptyWorkers.has(emptyNode, fullLabel))
                                    .forEach(emptyNode -> scattering.moveIfPossible(fullNode, emptyNode, mapLabel(fullLabel),
                                            amount));
                        }));

        // additional labels
        var additionalWorkers = labelScattering.getAdditional();
        fullWorkers.nodes()
                .forEach(fullNode -> fullWorkers.labels(fullNode)
                        .stream()
                        .filter(fullLabel -> additionalWorkers.countLabelInNodes(fullLabel) > 0)
                        .forEach(fullLabel -> {
                            double perClass = Math.min(additionalClassesPercent,
                                    maxAdditionalPercent / additionalWorkers.countLabelInNodes(fullLabel));
                            int amount = (int) Math.ceil(scattering.get(fullNode, mapLabel(fullLabel)) * perClass);

                            additionalWorkers.nodes()
                                    .stream()
                                    .filter(additionalNode -> additionalWorkers.has(additionalNode, fullLabel))
                                    .forEach(additionalNode -> scattering.moveIfPossible(fullNode, additionalNode,
                                            mapLabel(fullLabel), amount));
                        }));

        return scattering;
    }

    private String mapLabel(Integer label) {
        return labelMapping.get(label);
    }

}
