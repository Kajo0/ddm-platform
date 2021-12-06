package pl.edu.pw.ddm.platform.testing.interfaces.impl.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class LocalDatasetProcessor {

    private final boolean addIndex;
    private final boolean vectorizeStrings;
    private final Path dataPath;
    private final String separator;
    private final Integer idIndex;
    private final Integer labelIndex;

    private final Map<String, String> labelMapping = new HashMap<>();
    private final Map<String, String> vectorsMapping = new HashMap<>();
    private int vectorsMappingCounter = 0;

    @Getter
    private Integer attributesAmount;

    public void process() throws IOException {
        if (processed()) {
            return;
        }

        int[] i = new int[]{0};
        Stream<String> stream = Files.lines(dataPath)
                .filter(StringUtils::isNotBlank);

        if (addIndex) {
            stream = stream.map(l -> (i[0]++) + separator + l);
        }

        if (vectorizeStrings) {
            stream = stream.map(l -> l.split(separator))
                    .map(this::allNumeric)
                    .map(attrs -> String.join(separator, attrs));
        }

        String lines = stream.collect(Collectors.joining(System.lineSeparator()));
        Files.write(dataPath, lines.getBytes());

        attributesAmount = lines.substring(0, lines.indexOf(System.lineSeparator()))
                .split(separator).length;
        if (idIndex != null || addIndex) {
            --attributesAmount;
        }
        if (labelIndex != null) {
            --attributesAmount;
        }
    }

    private String[] allNumeric(String[] attributes) {
        for (int i = 0; i < attributes.length; ++i) {
            if (i == indexId()) {
                continue;
            } else if (i == indexLabel()) {
                String label = labelMapping.get(attributes[i]);
                if (label == null) {
                    if (isNumeric(attributes[i])) {
                        label = Optional.of(attributes[i])
                                .map(Double::parseDouble)
                                .map(Double::intValue)
                                .map(String::valueOf)
                                .orElse(attributes[i]);
                    } else {
                        label = String.valueOf(labelMapping.size());
                    }
                    labelMapping.put(attributes[i], label);
                }
                attributes[i] = label;
            } else if (!isNumeric(attributes[i])) {
                String value = vectorsMapping.get(attributes[i]);
                if (value == null) {
                    value = String.valueOf(vectorsMappingCounter++);
                    vectorsMapping.put(attributes[i], value);
                }
                attributes[i] = value;
            }
        }
        return attributes;
    }

    private int indexLabel() {
        if (labelIndex != null) {
            if (addIndex) {
                return labelIndex + 1;
            } else {
                return labelIndex;
            }
        } else {
            return -1;
        }
    }

    private int indexId() {
        if (idIndex != null) {
            return idIndex;
        } else if (addIndex) {
            return 0;
        } else {
            return -1;
        }
    }

    private boolean processed() {
        return !labelMapping.isEmpty();
    }

    private boolean isNumeric(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            try {
                Double.parseDouble(value);
                return true;
            } catch (NumberFormatException ex) {
                return false;
            }
        }
    }

}
