package pl.edu.pw.ddm.platform.core.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class PartitioningStrategyFacade {

    private final PartitionerStrategyLoader partitionerStrategyLoader;

    // FIXME change name everywhere to save?
    public String load(@NonNull PartitioningStrategyFacade.LoadRequest request) {
        return partitionerStrategyLoader.save(request.file);
    }

    // TODO remove debug
    public String info() {
        try {
            return new ObjectMapper().writeValueAsString(partitionerStrategyLoader.allStrategiesInfo());
        } catch (JsonProcessingException e) {
            return partitionerStrategyLoader.allStrategiesInfo()
                    .toString();
        }
    }

    @Value(staticConstructor = "of")
    public static class LoadRequest {

        @NonNull
        private final MultipartFile file;
    }

}
