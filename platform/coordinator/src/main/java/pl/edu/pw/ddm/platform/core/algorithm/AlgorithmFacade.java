package pl.edu.pw.ddm.platform.core.algorithm;

import lombok.Builder;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import pl.edu.pw.ddm.platform.core.instance.InstanceFacade;

@Service
public class AlgorithmFacade {

    private final InstanceFacade instanceFacade;
    private final AlgorithmLoader algorithmLoader;
    private final AlgorithmBroadcaster algorithmBroadcaster;

    AlgorithmFacade(InstanceFacade instanceFacade, InMemoryAlgorithmLoader algorithmLoader,
            DefaultAlgorithmBroadcaster algorithmBroadcaster) {
        this.instanceFacade = instanceFacade;
        this.algorithmLoader = algorithmLoader;
        this.algorithmBroadcaster = algorithmBroadcaster;
    }

    public String load(@NonNull LoadRequest request) {
        return algorithmLoader.load(request.name, request.jar);
    }

    public String broadcast(@NonNull BroadcastRequest request) {
        var req = InstanceFacade.AddressRequest.builder()
                .instanceId(request.instanceId)
                .build();
        var addr = instanceFacade.addresses(req);

        var alg = algorithmLoader.getAlgorithm(request.algorithmId);
        if (alg == null) {
            throw new IllegalArgumentException("No algorithm with id: " + request.algorithmId);
        }

        return algorithmBroadcaster.broadcast(addr, alg.getId(), alg.getJar());
    }

    @Builder
    public static class LoadRequest {

        private final String name;

        @NonNull
        private final byte[] jar;
    }

    @Builder
    public static class BroadcastRequest {

        @NonNull
        private final String instanceId;

        @NonNull
        private final String algorithmId;
    }

}
