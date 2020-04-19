package pl.edu.pw.ddm.platform.core.results.dto;

import java.util.Map;

import lombok.Data;

@Data
public class ValidationMetricsDto {

    private Map<String, Double> metrics;

}
