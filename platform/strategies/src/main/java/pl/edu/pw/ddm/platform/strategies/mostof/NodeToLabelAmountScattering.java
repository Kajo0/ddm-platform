package pl.edu.pw.ddm.platform.strategies.mostof;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;
import lombok.ToString;

@ToString
class NodeToLabelAmountScattering {

    private final Map<Integer, NodeClassAmount> mainMap = new HashMap<>();

    boolean put(int worker, String label, int amount) {
        return mainMap.computeIfAbsent(worker, k -> new NodeClassAmount())
                .put(label, amount) != null;
    }

    int get(int worker, String label) {
        var nca = mainMap.get(worker);
        if (nca == null) {
            return 0;
        } else {
            return nca.get(label);
        }
    }

    int moveIfPossible(int from, int to, String label, int amount) {
        if (get(from, label) < amount) {
            return 0;
        } else {
            return move(from, to, label, amount);
        }
    }

    int move(int from, int to, String label, int amount) {
        int present = get(from, label);
        Preconditions.checkState(present >= amount, "Cannot move more '%s' than present '%s'.", amount, present);
        remove(from, label, amount);
        add(to, label, amount);
        return amount;
    }

    boolean decrease(int worker, String label) {
        var amount = mainMap.get(worker).map.get(label);
        if (amount.intValue() <= 0) {
            return false;
        } else {
            amount.decrementAndGet();
            return true;
        }
    }

    boolean isEmpty() {
        return mainMap.values()
                .stream()
                .map(a -> a.map)
                .map(Map::values)
                .flatMap(Collection::stream)
                .allMatch(a -> a.intValue() <= 0);
    }

    private void add(int worker, String label, int amount) {
        mainMap.computeIfAbsent(worker, k -> new NodeClassAmount())
                .put(label, get(worker, label) + amount);
    }

    private void remove(int worker, String label, int amount) {
        mainMap.computeIfAbsent(worker, k -> new NodeClassAmount())
                .put(label, get(worker, label) - amount);
    }

    @ToString
    private static class NodeClassAmount {

        private static final AtomicInteger ZERO = new AtomicInteger(0);

        private final Map<String, AtomicInteger> map = new HashMap<>();

        AtomicInteger put(String label, Integer amount) {
            return map.put(label, new AtomicInteger(amount));
        }

        int get(String label) {
            return map.getOrDefault(label, ZERO)
                    .intValue();
        }
    }

}