package pl.edu.pw.ddm.platform.metrics.dto;

import lombok.Value;

@Value(staticConstructor = "of")
public class IdLabel {

    private final String id;
    private final String label;

}
