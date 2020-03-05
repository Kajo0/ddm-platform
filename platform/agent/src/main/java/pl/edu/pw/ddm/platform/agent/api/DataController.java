package pl.edu.pw.ddm.platform.agent.api;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("agent/data")
class DataController {

    @PostMapping("load/{dataId}")
    String load(@RequestParam("dataFile") MultipartFile dataFile, @PathVariable String dataId) {
        return "data loaded with id " + dataId;
    }

}
