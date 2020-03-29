package pl.edu.pw.ddm.platform.core.coordinator;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pw.ddm.platform.core.data.DataFacade;

@RestController
@RequestMapping("coordinator/command/data")
class DataCommandController {

    private final DataFacade dataFacade;

    DataCommandController(DataFacade dataFacade) {
        this.dataFacade = dataFacade;
    }

    // TODO add lazy loading used for scatter

    @PutMapping("load/uri")
    String loadDataFromUri(@RequestParam String dataUri,
                           @RequestParam String separator,
                           @RequestParam(required = false) Integer idIndex,
                           @RequestParam Integer labelIndex) {
        // TODO advance parametrization
        var req = DataFacade.LoadRequest.builder()
                .uri(dataUri)
                .separator(separator)
                .idIndex(idIndex)
                .labelIndex(labelIndex)
                .deductType(true)
                .build();
        return dataFacade.load(req);
    }

    @PostMapping("load/file")
    String loadDataFile(@RequestParam("dataFile") MultipartFile dataFile,
                        @RequestParam String separator,
                        @RequestParam(required = false) Integer idIndex,
                        @RequestParam Integer labelIndex) {
        // TODO advance parametrization
        var req = DataFacade.LoadRequest.builder()
                .file(dataFile)
                .separator(separator)
                .idIndex(idIndex)
                .labelIndex(labelIndex)
                .deductType(true)
                .build();
        return dataFacade.load(req);
    }

    @GetMapping("scatter/instance/{instanceId}/{strategy}/{dataId}")
    String scatterData(@PathVariable String instanceId, @PathVariable String strategy, @PathVariable String dataId) {
        // TODO advance parametrization
        var req = DataFacade.ScatterRequest.builder()
                .instanceId(instanceId)
                .strategy(strategy)
                .dataId(dataId)
                .build();
        return dataFacade.scatter(req);
    }

    @GetMapping(value = "info", produces = MediaType.APPLICATION_JSON_VALUE)
    String loadedDataInfo() {
        return dataFacade.info();
    }

}
