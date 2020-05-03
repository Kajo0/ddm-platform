package pl.edu.pw.ddm.platform.runner.models;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

import lombok.Data;
import org.apache.commons.lang3.time.DurationFormatUtils;

@Data
public class TimeStatistics implements Serializable {

    private LocalDateTime start;
    private LocalDateTime end;

    private long dataLoadingMillis;

    public Long duration() {
        return Duration.between(start, end).toMillis();
    }

    public Long withoutDataLoading() {
        return Duration.between(start, end).toMillis() - dataLoadingMillis;
    }

    public String durationPretty() {
        return DurationFormatUtils.formatDurationHMS(duration());
    }

    public String withoutDataLoadingPretty() {
        return DurationFormatUtils.formatDurationHMS(withoutDataLoading());
    }

}
