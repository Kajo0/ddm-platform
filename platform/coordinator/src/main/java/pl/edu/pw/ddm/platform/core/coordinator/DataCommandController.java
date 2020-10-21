package pl.edu.pw.ddm.platform.core.coordinator;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
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
import pl.edu.pw.ddm.platform.core.data.DistanceFunctionFacade;
import pl.edu.pw.ddm.platform.core.data.PartitioningStrategyFacade;

@RestController
@RequestMapping("coordinator/command/data")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class DataCommandController {

    private final DataFacade dataFacade;
    private final DistanceFunctionFacade distanceFunctionFacade;
    private final PartitioningStrategyFacade partitioningStrategyFacade;

    // TODO add lazy loading used for scatter

    @PutMapping("load/uri")
    String loadDataFromUri(@RequestParam String dataUri,
                           @RequestParam String separator,
                           @RequestParam(required = false) Integer idIndex,
                           @RequestParam Integer labelIndex,
                           @RequestParam(required = false, defaultValue = "false") Boolean vectorizeStrings,
                           @RequestParam(required = false, defaultValue = "true") boolean deductType) {
        // TODO advance parametrization
        var req = DataFacade.LoadRequest.builder()
                .uri(dataUri)
                .separator(separator)
                .idIndex(idIndex)
                .labelIndex(labelIndex)
                .deductType(deductType)
                .vectorizeStrings(vectorizeStrings)
                .build();
        return dataFacade.load(req);
    }

    @PostMapping("load/file")
    String loadDataFile(@RequestParam("dataFile") MultipartFile dataFile,
                        @RequestParam String separator,
                        @RequestParam(required = false) Integer idIndex,
                        @RequestParam Integer labelIndex,
                        @RequestParam(required = false, defaultValue = "false") Boolean vectorizeStrings,
                        @RequestParam(required = false, defaultValue = "true") boolean deductType) {
        // TODO advance parametrization eg attr types
        var req = DataFacade.LoadRequest.builder()
                .file(dataFile)
                .separator(separator)
                .idIndex(idIndex)
                .labelIndex(labelIndex)
                .deductType(deductType)
                .vectorizeStrings(vectorizeStrings)
                .build();
        return dataFacade.load(req);
    }

    @PostMapping("distance-function/load/file")
    String loadDistanceFunctionFile(@RequestParam("distanceFunctionFile") MultipartFile distanceFunctionFile) {
        var req = DistanceFunctionFacade.LoadRequest.of(distanceFunctionFile);
        return distanceFunctionFacade.load(req);
    }

    @PostMapping("scatter/{instanceId}/{dataId}")
    String scatterData(@PathVariable String instanceId,
                       @PathVariable String dataId,
                       @RequestParam("strategy") String strategy,
                       @RequestParam(value = "distanceFunction", required = false) String distanceFunction,
                       @RequestParam(value = "strategyParams", required = false) String strategyParams,
                       @RequestParam("typeCode") String typeCode) {
        var req = DataFacade.ScatterRequest.builder()
                .instanceId(instanceId)
                .strategy(strategy)
                .distanceFunction(distanceFunction)
                .strategyParams(strategyParams)
                .dataId(dataId)
                .typeCode(typeCode)
                .build();
        return dataFacade.scatter(req);
    }

    @GetMapping("distance-function/broadcast/{instanceId}/{distanceFunctionId}")
    String broadcastDistanceFunction(@PathVariable String instanceId, @PathVariable String distanceFunctionId) {
        // TODO advance parametrization
        var req = DistanceFunctionFacade.BroadcastRequest.builder()
                .instanceId(instanceId)
                .distanceFunctionId(distanceFunctionId)
                .build();
        return distanceFunctionFacade.broadcast(req);
    }

    @PostMapping("partitioning-strategy/load/file")
    String loadPartitioningFile(@RequestParam("partitioningStrategyFile") MultipartFile partitioningStrategyFile) {
        var req = PartitioningStrategyFacade.LoadRequest.of(partitioningStrategyFile);
        return partitioningStrategyFacade.load(req);
    }

    @GetMapping(value = "info", produces = MediaType.APPLICATION_JSON_VALUE)
    String loadedDataInfo() {
        return dataFacade.info();
    }

    @GetMapping(value = "info/distance-functions", produces = MediaType.APPLICATION_JSON_VALUE)
    String loadedDistanceFunctions() {
        return distanceFunctionFacade.info();
    }

    @GetMapping(value = "info/partitioning-strategies", produces = MediaType.APPLICATION_JSON_VALUE)
    String loadedPartitioningStrategies() {
        return partitioningStrategyFacade.info();
    }

}
