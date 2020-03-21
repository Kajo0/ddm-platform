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
import pl.edu.pw.ddm.platform.interfaces.model.BaseModel;
import pl.edu.pw.ddm.platform.interfaces.model.LocalModel;

@UtilityClass
public class ModelPersister {

    private final static String PATH = "/models/";
    private final static String LOCAL = "local.data";

    // TODO add executionId
    @SneakyThrows
    public boolean saveLocal(BaseModel model) {
        boolean updated = false;

        Path path = Paths.get(PATH, LOCAL);
        Files.createDirectories(path.getParent());
        if (Files.exists(path)) {
            updated = removeLocal();
        }

        try (FileOutputStream fos = new FileOutputStream(path.toFile());
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(model);
        }

        return updated;
    }

    @SneakyThrows
    public LocalModel loadLocal() {
        Path path = Paths.get(PATH, LOCAL);
        if (Files.notExists(path)) {
            return null;
        } else {
            try (FileInputStream fis = new FileInputStream(path.toFile());
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                return (LocalModel) ois.readObject();
            }
        }
    }

    @SneakyThrows
    public boolean removeLocal() {
        Path path = Paths.get(PATH, LOCAL);
        return Files.deleteIfExists(path);
    }

}
