package pl.edu.pw.ddm.api.core.instance.dto;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Value
@Builder
public class ResultsDto {

    private String algorithm;
    private String type;
    private Integer nodes;
    private LocalDateTime start;
    private LocalDateTime finish;
    private Map<String, LocalResultsDto> localResults;
    private GlobalResultsDto results;

    public static ResultsDto dummy() {
        return ResultsDto.builder()
                .algorithm("aoptkm")
                .type("clustering")
                .nodes(4)
                .start(LocalDateTime.now().minusHours(3))
                .finish(LocalDateTime.now())
                .localResults(Map.of())
                .results(new GlobalResultsDto())
                .build();
    }

    @Data
    public static class LocalResultsDto {

        private Long processingTime;
        private Long dataDownload;
        private Long dataUpload;
        private Long externalDataDownload;
    }

    @Data
    public static class GlobalResultsDto {

        private Long processingTime;
        private Long dataTransfer;
        private String qualityMetric;
        private Double quality;
    }

}
