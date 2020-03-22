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

    private final static String PATH = "/execution/method.data";

    // TODO add executionId
    @SneakyThrows
    public boolean save(MiningMethod method) {
        boolean updated = false;

        Path path = Paths.get(PATH);
        Files.createDirectories(path.getParent());
        if (Files.exists(path)) {
            updated = removeLocal();
        }

        try (FileOutputStream fos = new FileOutputStream(path.toFile());
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(method);
        }

        return updated;
    }

    @SneakyThrows
    public MiningMethod load() {
        Path path = Paths.get(PATH);
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
    public boolean removeLocal() {
        Path path = Paths.get(PATH);
        return Files.deleteIfExists(path);
    }

}
