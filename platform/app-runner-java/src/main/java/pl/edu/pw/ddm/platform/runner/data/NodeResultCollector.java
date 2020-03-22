package pl.edu.pw.ddm.platform.runner.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Value;
import pl.edu.pw.ddm.platform.interfaces.data.ResultCollector;

public class NodeResultCollector implements ResultCollector {

    private final static String PATH = "/execution/results.txt";

    @Getter
    private List<NodeResultData> results = new LinkedList<>();

    @Override
    public void collect(String id, String result) {
        NodeResultData data = NodeResultData.of(id, result);
        results.add(data);
    }

    @Override
    public void collect(String id, double result) {
        collect(id, String.valueOf(result));
    }

    @SneakyThrows
    public void saveResults() {
        Path path = Paths.get(PATH);
        Files.createDirectories(path.getParent());
        Files.deleteIfExists(path);

        // TODO optimize
        String data = results.stream()
                .map(r -> r.getId() + "," + r.getValue())
                .collect(Collectors.joining("\n"));

        Files.write(path, data.getBytes());
    }

    @Value(staticConstructor = "of")
    public static class NodeResultData {

        private String id;
        private String value;
    }

}
