package pl.edu.pw.ddm.platform.strategies.conceptdrift;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
class DriftDivider {

    private final List<SeparatedBucket> sortedBuckets;
    private final int drifts;
    private final int singularitiesExtracted; // FIXME make it use

    private final HashMap<Integer, List<SeparatedBucket>> result = new HashMap<>();

    HashMap<Integer, List<SeparatedBucket>> divide() {
        var idAmount = sortedBuckets.stream()
                .mapToInt(SeparatedBucket::idsInside)
                .sum();

        var divisor = (int) Math.floor((double) (idAmount) / drifts);

        // FIXME avoid 1,1,1,1,|7777| situation
        var sum = 0;
        int number = 0;
        for (SeparatedBucket bucket : sortedBuckets) {
            result.computeIfAbsent(number, (s) -> new ArrayList<>())
                    .add(bucket);

            sum += bucket.idsInside();
            if (sum / divisor > 0) {
                sum = sum - divisor;
                ++number;
                number = Math.min(number, drifts - 1);
            }
        }

        return result;
    }

}
