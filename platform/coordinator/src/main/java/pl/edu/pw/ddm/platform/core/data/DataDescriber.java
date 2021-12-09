package pl.edu.pw.ddm.platform.core.data;

import static pl.edu.pw.ddm.platform.core.data.DataLoader.DataDesc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class DataDescriber {

    private static final long LINE_SAMPLES = 5;

    private final Path path;
    private final String id;
    private final String name;
    private final String separator;
    private final Integer idIndex;
    private final Integer labelIndex;
    private final boolean deductType;
    private final boolean allNumeric;

    DataDesc describe() throws IOException {
        var lines = countSamples();
        var type = FilenameUtils.getExtension(name);
        var size = path.toFile()
                .length();
        var attributesCount = countAttributes();
        var location = new DataDesc.DataLocation(List.of(path.toString()), List.of(size), List.of(lines));
        var types = deductAllTypes();

        return new DataDesc(id, name, type, size, lines, separator, idIndex, labelIndex, attributesCount, types,
                location);
    }

    private long countSamples() throws IOException {
        // TODO check if header and remove
        // TODO optimize if added index
        return Files.lines(path)
                .filter(Predicate.not(String::isBlank))
                .count();
    }

    public int countAttributes() throws IOException {
        int count = Files.lines(path)
                .findFirst()
                .map(l -> l.split(separator))
                .map(c -> c.length)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cannot count attributes by splitting first line using separator: " + separator));

        if (idIndex != null) {
            --count;
        }
        if (labelIndex != null) {
            --count;
        }

        return count;
    }

    private String[] deductAllTypes() throws IOException {
        if (deductType) {
            log.debug("Deducting type for data '{}'.", path.getFileName());
            return Files.lines(path)
                    .limit(LINE_SAMPLES)
                    .map(l -> l.split(separator))
                    .map(this::deductLine)
                    .reduce(this::reduce)
                    .orElseThrow(
                            () -> new IllegalArgumentException("Cannot infer attribute types from data file: " + path));
        }

        int columns = Files.lines(path)
                .limit(1)
                .map(l -> l.split(separator))
                .map(a -> a.length)
                .findFirst()
                .orElse(0);

        if (allNumeric) {
            String[] result = new String[columns];
            for (int i = 0; i < columns; ++i) {
                result[i] = "numeric";
            }
            return result;
        } else {
            return new String[columns];
        }
    }

    private String[] reduce(String[] first, String[] second) {
        String[] result = new String[first.length];
        for (int i = 0; i < first.length; ++i) {
            result[i] = first[i].equals(second[i]) ? first[i] : "nominal";
        }

        return result;
    }

    private String[] deductLine(String[] attributes) {
        String[] result = new String[attributes.length];
        for (int i = 0; i < attributes.length; ++i) {
            result[i] = deductAttr(attributes[i]);
        }
        return result;
    }

    private String deductAttr(String attr) {
        // TODO maybe constant with missing value -> '?' => numeric_with_missing etc
        if (isNumeric(attr)) {
            return "numeric";
        } else {
            return "nominal";
        }
    }

    static boolean isNumeric(String value) {
        value = value.trim();
        return Ints.tryParse(value) != null || Doubles.tryParse(value) != null;
    }

}
