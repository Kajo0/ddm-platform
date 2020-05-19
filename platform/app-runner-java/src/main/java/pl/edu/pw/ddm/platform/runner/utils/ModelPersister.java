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

    private final static String LOCAL = ControlFileNames.LOCAL_MODEL;

    @SneakyThrows
    public boolean saveLocal(String executionPath, BaseModel model, String executionId) {
        boolean updated = false;

        Path path = Paths.get(executionPath, executionId, LOCAL);
        Files.createDirectories(path.getParent());
        if (Files.exists(path)) {
            updated = removeLocal(executionPath, executionId);
        }

        try (FileOutputStream fos = new FileOutputStream(path.toFile());
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(model);
        }

        return updated;
    }

    @SneakyThrows
    public LocalModel loadLocal(String executionPath, String executionId) {
        Path path = Paths.get(executionPath, executionId, LOCAL);
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
    public boolean removeLocal(String executionPath, String executionId) {
        Path path = Paths.get(executionPath, executionId, LOCAL);
        return Files.deleteIfExists(path);
    }

}
