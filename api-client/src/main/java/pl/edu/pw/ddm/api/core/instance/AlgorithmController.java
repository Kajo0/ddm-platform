package pl.edu.pw.ddm.api.core.instance;

import java.util.UUID;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.ddm.api.core.instance.dto.AlgorithmProcessingInfoDto;
import pl.edu.pw.ddm.api.core.instance.dto.ProcessDataDto;
import pl.edu.pw.ddm.api.core.instance.dto.ResultsDto;

@Slf4j
@RestController
@RequestMapping("instance/{instanceId}/algorithm")
class AlgorithmController {

    @PostMapping("start")
    ProcessDataDto start(@RequestBody AlgorithmParams params) {
        log.debug("[TMP] starting algorithm with params: " + params);
        return new ProcessDataDto(UUID.randomUUID().toString(), ProcessType.Algorithm.START);
    }

    @GetMapping("{processId}/stop")
    ProcessDataDto stop(@PathVariable String processId) {
        log.debug("[TMP] stopping algorithm " + processId);
        return new ProcessDataDto(processId, ProcessType.Algorithm.STOP);
    }

    @GetMapping("{processId}/status")
    AlgorithmProcessingInfoDto status(@PathVariable String processId) {
        log.debug("[TMP] getting status algorithm " + processId);
        return AlgorithmProcessingInfoDto.dummy();
    }

    @GetMapping("{processId}/results")
    ResultsDto results(@PathVariable String processId) {
        log.debug("[TMP] getting results of algorithm " + processId);
        return ResultsDto.dummy();
    }

    @Data
    private static class AlgorithmParams {}

}
