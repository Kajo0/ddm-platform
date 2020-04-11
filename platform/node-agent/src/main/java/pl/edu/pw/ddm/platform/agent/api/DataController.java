package pl.edu.pw.ddm.platform.agent.api;

import java.io.IOException;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pw.ddm.platform.agent.data.DataLoader;
import pl.edu.pw.ddm.platform.agent.data.dto.DataDesc;

@RestController
@RequestMapping("agent/data")
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class DataController {

    private final DataLoader dataLoader;

    @PostMapping(value = "load")
    String load(@RequestParam("dataFile") MultipartFile dataFile,
                @RequestParam String dataId,
                @RequestParam String separator,
                @RequestParam Integer idIndex,
                @RequestParam Integer labelIndex,
                @RequestParam Integer attributesAmount,
                @RequestParam String colTypes) throws IOException {
        DataDesc desc = DataDesc.builder()
                .id(dataId)
                .separator(separator)
                .idIndex(idIndex)
                .labelIndex(labelIndex)
                .attributesAmount(attributesAmount)
                .colTypes(colTypes.split(","))
                .build();
        // TODO divide into train/test data
        dataLoader.save(dataFile.getBytes(), DataLoader.DataType.TRAIN, desc);
        return "ok";
    }

}
