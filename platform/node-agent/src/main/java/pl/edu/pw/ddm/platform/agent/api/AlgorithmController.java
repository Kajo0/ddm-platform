package pl.edu.pw.ddm.platform.agent.api;

import java.io.IOException;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pw.ddm.platform.agent.algorithm.AlgorithmLoader;
import pl.edu.pw.ddm.platform.agent.algorithm.dto.AlgorithmDesc;

@RestController
@RequestMapping("agent/algorithm")
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class AlgorithmController {

    private final AlgorithmLoader algorithmLoader;

    @PostMapping(value = "load")
    String load(@RequestParam("algorithmFile") MultipartFile algorithmFile,
                @RequestParam("algorithmId") String algorithmId,
                @RequestParam("algorithmPackageName") String algorithmPackageName) throws IOException {
        AlgorithmDesc desc = AlgorithmDesc.builder()
                .id(algorithmId)
                .packageName(algorithmPackageName)
                .build();
        algorithmLoader.save(algorithmFile.getBytes(), desc);
        return "ok";
    }

}
