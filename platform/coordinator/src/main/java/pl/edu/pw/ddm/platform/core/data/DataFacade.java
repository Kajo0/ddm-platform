package pl.edu.pw.ddm.platform.core.data;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pw.ddm.platform.core.data.dto.DataDescDto;
import pl.edu.pw.ddm.platform.core.instance.InstanceFacade;

@Service
public class DataFacade {

    private final InstanceFacade instanceFacade;
    private final DataLoader dataLoader;
    private final DataPartitioner dataPartitioner;

    DataFacade(InstanceFacade instanceFacade, DataLoader dataLoader, DataPartitioner dataPartitioner) {
        this.instanceFacade = instanceFacade;
        this.dataLoader = dataLoader;
        this.dataPartitioner = dataPartitioner;
    }

    public String load(@NonNull LoadRequest request) {
        if (request.uri != null) {
            return dataLoader.save(request.uri, request.separator, request.idIndex, request.labelIndex, request.deductType);
        } else if (request.file != null) {
            return dataLoader.save(request.file, request.separator, request.idIndex, request.labelIndex, request.deductType);
        } else {
            throw new IllegalStateException("No URI or file provided to load.");
        }
    }

    public String scatter(@NonNull ScatterRequest request) {
        var req = InstanceFacade.AddressRequest.of(request.instanceId);
        var addr = instanceFacade.addresses(req);

        var data = dataLoader.getDataDesc(request.dataId);
        if (data == null) {
            throw new IllegalArgumentException("No data with id: " + request.dataId);
        }

        return dataPartitioner.scatter(addr, data, request.strategy);
    }

    public DataDescDto description(@NonNull DescriptionRequest request) {
        // TODO more checks
        return Optional.of(request)
                .map(DescriptionRequest::getDataId)
                .map(dataLoader::getDataDesc)
                .map(DataDescMapper.INSTANCE::map)
                .get();
    }

    // TODO remove debug
    public String info() {
        try {
            return new ObjectMapper().writeValueAsString(dataLoader.allDataInfo());
        } catch (JsonProcessingException e) {
            return dataLoader.allDataInfo()
                    .toString();
        }
    }

    @Builder
    public static class LoadRequest {

        private final String uri;
        private final MultipartFile file;

        @NonNull
        private final String separator;

        private final Integer idIndex;
        private final Integer labelIndex;

        @Builder.Default
        private final boolean deductType = true;
    }

    @Builder
    public static class ScatterRequest {

        @NonNull
        private final String instanceId;

        @NonNull
        private final String dataId;

        @NonNull
        private final String strategy;
    }

    @Value(staticConstructor = "of")
    public static class DescriptionRequest {

        @NonNull
        private final String dataId;
    }

}
