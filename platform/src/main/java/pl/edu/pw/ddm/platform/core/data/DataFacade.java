package pl.edu.pw.ddm.platform.core.data;

import lombok.Builder;
import lombok.NonNull;
import org.springframework.stereotype.Service;
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
        return dataLoader.load(request.uri, request.deductType);
    }

    public String scatter(@NonNull ScatterRequest request) {
        var req = InstanceFacade.AddressRequest.builder()
                .instanceId(request.instanceId)
                .build();
        var addr = instanceFacade.addresses(req);

        var data = dataLoader.getDataDesc(request.dataId);
        if (data == null) {
            throw new IllegalArgumentException("No data with id: " + request.dataId);
        }

        return dataPartitioner.scatter(addr, data, request.strategy);
    }

    @Builder
    public static class LoadRequest {

        @NonNull
        private final String uri;

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

}
