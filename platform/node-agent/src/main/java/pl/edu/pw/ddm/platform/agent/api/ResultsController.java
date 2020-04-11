package pl.edu.pw.ddm.platform.agent.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.ddm.platform.agent.results.ResultsLoader;

@RestController
@RequestMapping("agent/results")
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class ResultsController {

    private final ResultsLoader resultsLoader;

    @GetMapping(value = "{executionId}/download", produces = MediaType.TEXT_PLAIN_VALUE)
    ResponseEntity<byte[]> download(@PathVariable String executionId) throws IOException {
        File file = resultsLoader.load(executionId);
        if (file == null) {
            return ResponseEntity.notFound().build();
        } else {
            byte[] bytes = Files.readAllBytes(file.toPath());
            return ResponseEntity.ok(bytes);
        }
    }

}
