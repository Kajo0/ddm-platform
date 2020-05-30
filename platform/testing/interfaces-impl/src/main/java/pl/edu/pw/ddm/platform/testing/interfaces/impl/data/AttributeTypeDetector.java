package pl.edu.pw.ddm.platform.testing.interfaces.impl.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.RequiredArgsConstructor;
import pl.edu.pw.ddm.platform.interfaces.data.DataProvider;

@RequiredArgsConstructor
class AttributeTypeDetector {

    private static final int LINE_SAMPLES = 30;

    private final String dataPath;
    private final DataProvider.DataDesc dataDesc;

    String[] detect() throws IOException {
        Path path = Paths.get(dataPath);
        return Files.lines(path)
                .limit(LINE_SAMPLES)
                .map(l -> l.split(dataDesc.getSeparator()))
                .map(this::deductLine)
                .reduce(this::reduce)
                .orElseThrow(() -> new IllegalArgumentException("Cannot infer attribute types from data file: " + path));
    }

    private String[] deductLine(String[] attributes) {
        String[] result = new String[attributes.length];
        for (int i = 0; i < attributes.length; ++i) {
            result[i] = deductAttr(attributes[i]);
        }
        return result;
    }

    private String deductAttr(String attr) {
        try {
            Integer.valueOf(attr);
            return DataProvider.AttributeType.NUMERIC;
        } catch (NumberFormatException e) {
            // ignore
        }

        try {
            Double.valueOf(attr);
            return DataProvider.AttributeType.NUMERIC;
        } catch (NumberFormatException e) {
            // ignore
        }

        return DataProvider.AttributeType.NOMINAL;
    }

    private String[] reduce(String[] first, String[] second) {
        String[] result = new String[first.length];
        for (int i = 0; i < first.length; ++i) {
            result[i] = first[i].equals(second[i]) ? first[i] : DataProvider.AttributeType.NOMINAL;
        }

        return result;
    }

}
