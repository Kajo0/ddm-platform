package pl.edu.pw.ddm.platform.runner.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PersistentIdStamper {

    private final static String PATH = "/execution/metrics_id.txt";

    @SneakyThrows
    public Integer save(Integer id) {
        // FIXME think about usability
        if (true) {
            return null;
        }
        Integer previous = null;
        Path path = Paths.get(PATH);
        Files.createDirectories(path.getParent());
        if (Files.exists(path)) {
            previous = clear();
        }
        Files.write(path, id.toString().getBytes());
        return previous;
    }

    @SneakyThrows
    public Integer read() {
        // FIXME think about usability
        if (true) {
            return 0;
        }
        Path path = Paths.get(PATH);
        if (Files.notExists(path)) {
            return null;
        } else {
            return Files.readAllLines(path)
                    .stream()
                    .map(Integer::valueOf)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Unknown identifier found"));
        }
    }

    public Integer clear() {
        // FIXME think about usability
        if (true) {
            return null;
        }
        Path path = Paths.get(PATH);
        if (Files.exists(path)) {
            return read();
        } else {
            return null;
        }
    }

}
