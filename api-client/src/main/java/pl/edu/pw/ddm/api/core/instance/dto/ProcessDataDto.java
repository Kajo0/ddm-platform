package pl.edu.pw.ddm.api.core.instance.dto;

import lombok.NonNull;
import lombok.Value;

@Value
public class ProcessDataDto {

    private String id;
    private String type;

    public static ProcessDataDto dummy(@NonNull String type) {
        return new ProcessDataDto("null", type);
    }

}
