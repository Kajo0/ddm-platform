package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.dataset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.point.ObjectPoint;

public class ObjectDatasetReader {

    public static final String SPLIT_PATTERN = "\\s+";

    public static String[] getLineVal(String line, String splitPattern) {
        return Stream.of(line.split(splitPattern)).map(String::trim).toArray(String[]::new);
    }

    private static boolean[] deductNumeric(String[] stringVal) {
        boolean[] numeric = new boolean[stringVal.length];

        for (int i = 0; i < numeric.length; ++i) {
            numeric[i] = Doubles.tryParse(stringVal[i]) != null;
        }

        return numeric;
    }

    private static void readLine(Integer index, List<ObjectPoint> result, String line, boolean[] numeric,
                                 Integer indexNumber, Integer labelNumber, String splitPattern) {
        String[] stringVal = getLineVal(line, splitPattern);

        int length = stringVal.length;
        length -= indexNumber == null ? 0 : 1;
        length -= labelNumber == null ? 0 : 1;

        index = indexNumber == null ? index : Integer.valueOf(stringVal[indexNumber]);
        Integer label = labelNumber == null ? null : Integer.valueOf(stringVal[labelNumber]);

        Object[] tmpPoint = new Object[length];
        AtomicInteger atomic = new AtomicInteger(0);
        IntStream.range(0, stringVal.length)
                .filter(i -> !Objects.equals(i, indexNumber))
                .filter(i -> !Objects.equals(i, labelNumber))
                .sequential()
                .forEach(i -> {
                    int next = atomic.getAndIncrement();
                    if (numeric[i]) {
                        tmpPoint[next] = Double.valueOf(stringVal[i]);
                    } else {
                        tmpPoint[next] = stringVal[i];
                    }
                });

        ObjectPoint point = new ObjectPoint(tmpPoint, index);
        point.originalLabel = label;
        result.add(point);
    }

    public static List<ObjectPoint> read(String path, boolean[] numeric, Integer indexNumber, Integer labelNumber) {
        return read(path, numeric, indexNumber, labelNumber, SPLIT_PATTERN);
    }

    public static List<ObjectPoint> read(String path, boolean[] numeric, Integer indexNumber, Integer labelNumber,
                                         String splitPattern) {
        List<ObjectPoint> result = Lists.newArrayList();

        try {
            Path p = Paths.get(path);
            Iterator<String> it = Files.lines(p).iterator();
            int index = 0;
            String line;
            while (it.hasNext()) {
                line = it.next();
                if (!line.trim().isEmpty()) {
                    if (numeric == null) {
                        String[] stringVal = getLineVal(line, splitPattern);
                        numeric = deductNumeric(stringVal);
                    }

                    readLine(index, result, line, numeric, indexNumber, labelNumber, splitPattern);
                }
                ++index;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

}
