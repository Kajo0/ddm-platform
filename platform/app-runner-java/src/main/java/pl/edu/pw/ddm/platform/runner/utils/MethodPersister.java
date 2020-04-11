package pl.edu.pw.ddm.platform.runner.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import pl.edu.pw.ddm.platform.interfaces.mining.MiningMethod;

@UtilityClass
public class MethodPersister {

    private final static String DIR = "/ddm/execution";
    private final static String FILE = "method.data";

    @SneakyThrows
    public boolean save(MiningMethod method, String executionId) {
        boolean updated = false;

        Path path = Paths.get(DIR, executionId, FILE);
        Files.createDirectories(path.getParent());
        if (Files.exists(path)) {
            updated = removeLocal(executionId);
        }

        try (FileOutputStream fos = new FileOutputStream(path.toFile());
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(method);
        }

        return updated;
    }

    @SneakyThrows
    public MiningMethod load(String executionId) {
        Path path = Paths.get(DIR, executionId, FILE);
        if (Files.notExists(path)) {
            return null;
        } else {
            try (FileInputStream fis = new FileInputStream(path.toFile());
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                return (MiningMethod) ois.readObject();
            }
        }
    }

    @SneakyThrows
    public boolean removeLocal(String executionId) {
        Path path = Paths.get(DIR, executionId, FILE);
        return Files.deleteIfExists(path);
    }

}
