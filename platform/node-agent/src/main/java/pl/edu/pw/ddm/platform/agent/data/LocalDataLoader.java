package pl.edu.pw.ddm.platform.agent.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.pw.ddm.platform.agent.data.dto.DataDesc;

@Slf4j
@Service
public class LocalDataLoader implements DataLoader {

    private static final String DATA_PATH = "/execution/data";

    LocalDataLoader() throws IOException {
        Files.createDirectories(Paths.get(DATA_PATH));
    }

    @SneakyThrows
    @Override
    public boolean save(byte[] bytes, DataType type, DataDesc dataDesc) {
        Path path = Paths.get(DATA_PATH, dataDesc.getId(), type.getCode());
        Files.createDirectories(path.getParent());

        boolean exist = Files.deleteIfExists(path);
        if (exist) {
            log.info("Previous data file deleted.");
        }

        log.info("Saving {} data with id '{}'.", type, dataDesc.getId());
        Files.write(path, bytes);
        saveDescription(dataDesc);

        return exist;
    }

    private void saveDescription(DataDesc dataDesc) throws IOException {
        Properties prop = new Properties();
        prop.setProperty(DescriptionKey.SEPARATOR.getCode(), dataDesc.getSeparator());
        prop.setProperty(DescriptionKey.ID_INDEX.getCode(), String.valueOf(dataDesc.getIdIndex()));
        prop.setProperty(DescriptionKey.LABEL_INDEX.getCode(), String.valueOf(dataDesc.getLabelIndex()));
        prop.setProperty(DescriptionKey.ATTRIBUTES_AMOUNT.getCode(), String.valueOf(dataDesc.getAttributesAmount()));
        prop.setProperty(DescriptionKey.COLUMNS_TYPES.getCode(), String.join(",", dataDesc.getColTypes()));

        Path path = Paths.get(DATA_PATH, dataDesc.getId(), DataType.DESCRIPTION.getCode());
        prop.store(Files.newOutputStream(path), null);
    }

}
