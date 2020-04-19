package pl.edu.pw.ddm.platform.metrics.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class MetricsSummary {

    @Getter
    private Map<String, Double> metrics = new HashMap<>();

    public Double set(String metric, Double value) {
        return metrics.put(metric, value);
    }

    public Double get(String metric) {
        return metrics.get(metric);
    }

    public Double get(String metric, Supplier<Double> calculator) {
        Double value = metrics.get(metric);
        if (value == null) {
            value = calculator.get();
            set(metric, value);
        }
        return value;
    }

}
