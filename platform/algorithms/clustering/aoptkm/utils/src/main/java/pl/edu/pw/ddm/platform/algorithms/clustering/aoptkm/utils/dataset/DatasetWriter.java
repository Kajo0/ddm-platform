package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.dataset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.point.ObjectPoint;

public class DatasetWriter {

    public static void write(String path, Map<Integer, List<ObjectPoint>> clusters) {
        StringBuilder str = new StringBuilder();
        for (List<ObjectPoint> points : clusters.values()) {
            if (!points.isEmpty()) {
                for (int i = 0; i < points.get(0).values.length; ++i) {
                    final int finalI = i;
                    points.stream().forEach(point -> str.append(point.values[finalI] + "\t"));
                    str.append("\n");
                }
            } else {
                str.append("\n\n");
            }
        }
        try {
            Path p = Paths.get(path);
            Files.write(p, str.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
