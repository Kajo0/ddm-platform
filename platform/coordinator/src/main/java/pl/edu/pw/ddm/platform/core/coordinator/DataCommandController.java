package pl.edu.pw.ddm.platform.core.coordinator;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pw.ddm.platform.core.data.DataFacade;

@RestController
@RequestMapping("coordinator/command/data")
class DataCommandController {

    private final DataFacade dataFacade;

    DataCommandController(DataFacade dataFacade) {
        this.dataFacade = dataFacade;
    }

    @GetMapping("load/{dataUri}")
    String loadData(@PathVariable String dataUri) {
        // TODO advance parametrization
        var req = DataFacade.LoadRequest.builder()
                .uri(dataUri)
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

}
