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
    private final static String GLOBAL = ControlFileNames.GLOBAL_MODEL;

    @SneakyThrows
    public boolean saveLocal(String executionPath, BaseModel model, int stageIndex, String executionId) {
        boolean updated = false;

        Path path = Paths.get(executionPath, executionId, LOCAL + stageIndex);
        Files.createDirectories(path.getParent());
        if (Files.exists(path)) {
            updated = removeLocal(executionPath, LOCAL + stageIndex, executionId);
        }

        try (FileOutputStream fos = new FileOutputStream(path.toFile());
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(model);
        }

        return updated;
    }

    @SneakyThrows
    public boolean saveGlobal(String executionPath, BaseModel model, int stageIndex, String executionId) {
        boolean updated = false;

        Path path = Paths.get(executionPath, executionId, GLOBAL + stageIndex);
        Files.createDirectories(path.getParent());
        if (Files.exists(path)) {
            updated = removeLocal(executionPath, GLOBAL + stageIndex, executionId);
        }

        try (FileOutputStream fos = new FileOutputStream(path.toFile());
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(model);
        }

        return updated;
    }

    @SneakyThrows
    public LocalModel loadLastLocal(String executionPath, int stageIndex, String executionId) {
        for (int prev = stageIndex; prev >= 0; --prev) {
            Path path = Paths.get(executionPath, executionId, LOCAL + prev);
            if (Files.exists(path)) {
                try (FileInputStream fis = new FileInputStream(path.toFile());
                     ObjectInputStream ois = new ObjectInputStream(fis)) {
                    return (LocalModel) ois.readObject();
                }
            }
        }
        return null;
    }

    @SneakyThrows
    public boolean removeLocal(String executionPath, String modelType, String executionId) {
        Path path = Paths.get(executionPath, executionId, modelType);
        return Files.deleteIfExists(path);
    }

}
