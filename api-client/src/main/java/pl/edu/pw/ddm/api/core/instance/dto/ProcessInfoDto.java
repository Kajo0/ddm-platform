package pl.edu.pw.ddm.api.core.instance.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class ProcessInfoDto {

    private String processId;
    private String type;
    private LocalDateTime lastUpdate;
    private Boolean success;
    private String error;

    public static ProcessInfoDto.ProcessInfoDtoBuilder dummy(@NonNull String type) {
        return ProcessInfoDto.builder()
                .processId("dummy")
                .type(type)
                .lastUpdate(LocalDateTime.now())
                .success(false)
                .error(null);
    }

    public static ProcessInfoDto dummy() {
        return dummy("dummy").build();
    }

}
