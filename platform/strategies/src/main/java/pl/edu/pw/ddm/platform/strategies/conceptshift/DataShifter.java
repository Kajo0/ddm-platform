package pl.edu.pw.ddm.platform.strategies.conceptshift;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
class DataShifter {

    private static final String SHIFT_PREFIX = "8000";

    private final int workers;
    private final int shifts;
    private final String label;
    private final List<String> oneLabelData;
    private final Random rand;

    private final Map<String, NodeLabel> idToNode = new HashMap<>();
    private boolean processed;

    Map<String, NodeLabel> shift() {
        if (processed) {
            return idToNode;
        } else {
            processed = true;
        }

        int size = oneLabelData.size();
        int partCount = size / workers;
        Preconditions.checkState(partCount > 1, "To less data (count=%s) for %s shifts.", size, shifts);
        log.info("Shifting param={} within {} workers where data part={} ({}/{}) samples of data labelled with '{}'.",
                shifts, workers, partCount, size, workers, label);

        Collections.shuffle(oneLabelData, rand);
        int j = 0;
        for (int i = 0; i < size; ++i) {
            var id = oneLabelData.get(i);
            var shiftedLabel = label;
            if (j < shifts) {
                shiftedLabel = shiftLabel(j);
            }
            idToNode.put(id, new NodeLabel(j, shiftedLabel));

            if ((i + 1) % partCount == 0 && j < workers - 1) {
                ++j;
            }
        }

        return idToNode;
    }

    private String shiftLabel(int shift) {
        // TODO check all labels to avoid collision
        return SHIFT_PREFIX + shift + label;
    }


    @Value
    static class NodeLabel {
        Integer node;
        String label;
    }

}
