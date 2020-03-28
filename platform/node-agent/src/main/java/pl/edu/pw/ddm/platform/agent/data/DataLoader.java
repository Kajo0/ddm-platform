package pl.edu.pw.ddm.platform.agent.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DataLoader {

    private static final String DATA_PATH = "/execution/data";

    DataLoader() throws IOException {
        Files.createDirectories(Paths.get(DATA_PATH));
    }

    @SneakyThrows
    public boolean save(String dataId, byte[] bytes, DataType type) {
        Path path = Paths.get(DATA_PATH, dataId, type.getCode());
        Files.createDirectories(path.getParent());

        boolean exist = Files.deleteIfExists(path);
        if (exist) {
            log.info("Previous data file deleted.");
        }

        Files.write(path, bytes);
        log.info("Saving {} data with id '{}'.", type, dataId);

        return exist;
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum DataType {
        TRAIN("train"),
        TEST("test");

        private String code;
    }

}
