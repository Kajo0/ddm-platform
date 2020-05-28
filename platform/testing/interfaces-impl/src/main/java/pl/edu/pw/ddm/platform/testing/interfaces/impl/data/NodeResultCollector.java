package pl.edu.pw.ddm.platform.testing.interfaces.impl.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import pl.edu.pw.ddm.platform.interfaces.data.ResultCollector;

@RequiredArgsConstructor
public class NodeResultCollector implements ResultCollector {

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
    public void saveResults(String destPath) {
        Path path = Paths.get(destPath);
        Files.createDirectories(path.getParent());
        Files.deleteIfExists(path);

        String data = results.stream()
                .map(r -> r.getId() + "," + r.getValue())
                .collect(Collectors.joining("\n"));

        Files.write(path, data.getBytes());
    }

    @Value(staticConstructor = "of")
    public static class NodeResultData {

        String id;
        String value;
    }

}
