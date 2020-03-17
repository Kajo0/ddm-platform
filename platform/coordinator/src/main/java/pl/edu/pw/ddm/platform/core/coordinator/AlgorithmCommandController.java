package pl.edu.pw.ddm.platform.core.coordinator;

import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pw.ddm.platform.core.algorithm.AlgorithmFacade;

@RestController
@RequestMapping("coordinator/command/algorithm")
class AlgorithmCommandController {

    private final AlgorithmFacade algorithmFacade;

    AlgorithmCommandController(AlgorithmFacade algorithmFacade) {
        this.algorithmFacade = algorithmFacade;
    }

    @SneakyThrows
    @PostMapping("load")
    String loadAlgorithm(@RequestParam("file") MultipartFile file) {
        Preconditions.checkState(MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE.equals(file.getContentType()),
                "File is not octet-stream");
        Preconditions.checkState(FilenameUtils.isExtension(file.getOriginalFilename(), "jar"), "File is not .jar");

        var req = AlgorithmFacade.LoadRequest.builder()
                .name(file.getOriginalFilename())
                .jar(file.getBytes())
                .build();
        return algorithmFacade.load(req);
    }

    @GetMapping("broadcast/instance/{instanceId}/{algorithmId}")
    String broadcastAlgorithm(@PathVariable String instanceId, @PathVariable String algorithmId) {
        var req = AlgorithmFacade.BroadcastRequest.builder()
                .instanceId(instanceId)
                .algorithmId(algorithmId)
                .build();
        return algorithmFacade.broadcast(req);
    }

}
