package pl.edu.pw.ddm.platform.agent.algorithm;

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
import pl.edu.pw.ddm.platform.agent.algorithm.dto.AlgorithmDesc;

@Slf4j
@Service
class LocalAlgorithmLoader implements AlgorithmLoader {

    // TODO properties
    private static final String ALGORITHMS_PATH = "/ddm/algorithms";
    private static final String DESCRIPTION_FILENAME = "desc";

    @SneakyThrows
    @Override
    public boolean save(byte[] bytes, AlgorithmDesc algorithmDesc) {
        Path path = Paths.get(ALGORITHMS_PATH, algorithmDesc.getId(), algorithmDesc.getId());
        boolean exist = Files.exists(path.getParent());
        if (exist) {
            log.info("Previous algorithm file deleted.");
        }
        FileUtils.deleteDirectory(path.getParent().toFile());
        Files.createDirectories(path.getParent());

        log.info("Saving algorithm file with id '{}'.", algorithmDesc.getId());
        Files.write(path, bytes);
        saveDescription(algorithmDesc);

        return exist;
    }

    @Override
    public Path pathToFile(String algorithmId) {
        log.info("Loading algorithm file path with id '{}'.", algorithmId);
        Path path = Paths.get(ALGORITHMS_PATH, algorithmId, algorithmId);
        Preconditions.checkState(Files.exists(path), "Algorithm file with id %s not found.", algorithmId);
        return path;
    }

    @Override
    public AlgorithmDesc description(String algorithmId) {
        return loadDescription(algorithmId);
    }

    private void saveDescription(AlgorithmDesc algorithmDesc) throws IOException {
        Properties prop = new Properties();
        prop.setProperty(DescriptionKey.ID.getCode(), algorithmDesc.getId());
        prop.setProperty(DescriptionKey.PACKAGE.getCode(), algorithmDesc.getPackageName());

        Path path = Paths.get(ALGORITHMS_PATH, algorithmDesc.getId(), DESCRIPTION_FILENAME);
        prop.store(Files.newOutputStream(path), null);
    }

    @SneakyThrows
    private AlgorithmDesc loadDescription(String algorithmId) {
        Properties prop = new Properties();
        File file = Paths.get(ALGORITHMS_PATH, algorithmId, DESCRIPTION_FILENAME).toFile();
        try (FileInputStream fis = new FileInputStream(file)) {
            prop.load(fis);
        }

        return AlgorithmDesc.builder()
                .id(prop.getProperty(DescriptionKey.ID.getCode()))
                .packageName(prop.getProperty(DescriptionKey.PACKAGE.getCode()))
                .build();
    }

    @PostConstruct
    void init() throws IOException {
        Files.createDirectories(Paths.get(ALGORITHMS_PATH));
    }

}
