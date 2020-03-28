package pl.edu.pw.ddm.platform.agent.api;

import java.io.IOException;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pw.ddm.platform.agent.data.DataLoader;

@RestController
@RequestMapping("agent/data")
class DataController {

    private final DataLoader dataLoader;

    DataController(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    @PostMapping("load/{dataId}")
    String load(@RequestParam("dataFile") MultipartFile dataFile, @PathVariable String dataId) throws IOException {
        // TODO divide into train/test data
        dataLoader.save(dataId, dataFile.getBytes(), DataLoader.DataType.TRAIN);
        return "ok";
    }

}
