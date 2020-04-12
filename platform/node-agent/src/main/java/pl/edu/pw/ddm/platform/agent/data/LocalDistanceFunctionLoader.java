package pl.edu.pw.ddm.platform.agent.data;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import pl.edu.pw.ddm.platform.agent.data.dto.DistanceFunctionDesc;

@Slf4j
@Service
class LocalDistanceFunctionLoader implements DistanceFunctionLoader {

    // TODO properties
    private static final String FUNCTIONS_PATH = "/ddm/distance_functions";
    private static final String DESCRIPTION_FILENAME = "desc";

    @SneakyThrows
    @Override
    public boolean save(byte[] bytes, DistanceFunctionDesc distanceFunctionDesc) {
        Path path = Paths.get(FUNCTIONS_PATH, distanceFunctionDesc.getId(), distanceFunctionDesc.getId());
        boolean exist = Files.exists(path.getParent());
        if (exist) {
            log.info("Previous distance function file deleted.");
        }
        FileUtils.deleteDirectory(path.getParent().toFile());
        Files.createDirectories(path.getParent());

        log.info("Saving distance function '{}' with id '{}'.", distanceFunctionDesc.getFunctionName(), distanceFunctionDesc.getId());
        Files.write(path, bytes);
        saveDescription(distanceFunctionDesc);

        return exist;
    }

    @Override
    public Path pathToFile(String distanceFunctionId) {
        log.info("Loading distance function file with id '{}'.", distanceFunctionId);
        Path path = Paths.get(FUNCTIONS_PATH, distanceFunctionId, distanceFunctionId);
        Preconditions.checkState(Files.exists(path), "Distance function file with id %s not found.", distanceFunctionId);
        return path;
    }

    @Override
    public DistanceFunctionDesc description(String distanceFunctionId) {
        return loadDescription(distanceFunctionId);
    }

    private void saveDescription(DistanceFunctionDesc distanceFunctionDesc) throws IOException {
        Properties prop = new Properties();
        prop.setProperty(DescriptionKey.ID.getCode(), distanceFunctionDesc.getId());
        prop.setProperty(DescriptionKey.PACKAGE.getCode(), distanceFunctionDesc.getPackageName());
        prop.setProperty(DescriptionKey.FUNCTION_NAME.getCode(), distanceFunctionDesc.getFunctionName());

        Path path = Paths.get(FUNCTIONS_PATH, distanceFunctionDesc.getId(), DESCRIPTION_FILENAME);
        prop.store(Files.newOutputStream(path), null);
    }

    @SneakyThrows
    private DistanceFunctionDesc loadDescription(String distanceFunctionId) {
        Properties prop = new Properties();
        File file = Paths.get(FUNCTIONS_PATH, distanceFunctionId, DESCRIPTION_FILENAME).toFile();
        try (FileInputStream fis = new FileInputStream(file)) {
            prop.load(fis);
        }

        return DistanceFunctionDesc.builder()
                .id(prop.getProperty(DescriptionKey.ID.getCode()))
                .packageName(prop.getProperty(DescriptionKey.PACKAGE.getCode()))
                .functionName(prop.getProperty(DescriptionKey.FUNCTION_NAME.getCode()))
                .build();
    }

    @PostConstruct
    void init() throws IOException {
        Files.createDirectories(Paths.get(FUNCTIONS_PATH));
    }

}
