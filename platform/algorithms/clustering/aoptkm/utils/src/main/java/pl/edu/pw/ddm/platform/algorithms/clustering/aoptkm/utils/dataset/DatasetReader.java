package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.dataset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.point.DoublePoint;

public class DatasetReader {

    private static final String SPLIT_PATTERN = "\\s+";

    private static void readLine(Integer index, List<DoublePoint> result, String line, boolean firstIsIndex,
                                 boolean lastIsLabel) {
        String[] stringVal = line.split(SPLIT_PATTERN);
        int length = stringVal.length;
        length -= firstIsIndex ? 1 : 0;
        length -= lastIsLabel ? 1 : 0;
        double[] tmpPoint = new double[length];
        if (firstIsIndex) {
            index = Integer.valueOf(stringVal[0]);
        }
        int minusIndex = firstIsIndex ? 1 : 0;
        for (int i = minusIndex; i < length + minusIndex; ++i) {
            tmpPoint[i - minusIndex] = Double.valueOf(stringVal[i]);
        }
        result.add(new DoublePoint(tmpPoint, index));
    }

    public static List<DoublePoint> read(String path, boolean firstIsIndex, boolean lastIsLabel) {
        List<DoublePoint> result = Lists.newArrayList();

        try {
            Path p = Paths.get(path);
            Iterator<String> it = Files.lines(p).iterator();
            int index = 0;
            String line;
            while (it.hasNext()) {
                line = it.next();
                if (line.trim().length() > 0) {
                    readLine(index, result, line, firstIsIndex, lastIsLabel);
                }
                ++index;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Remember to close InputStream
     */
    public static List<DoublePoint> read(InputStream is, boolean firstIsIndex, boolean lastIsLabel) {
        List<DoublePoint> result = Lists.newArrayList();

        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            int index = 0;
            while ((line = r.readLine()) != null) {
                if (line.trim().length() > 0) {
                    readLine(index, result, line, firstIsIndex, lastIsLabel);
                }
                ++index;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

}
