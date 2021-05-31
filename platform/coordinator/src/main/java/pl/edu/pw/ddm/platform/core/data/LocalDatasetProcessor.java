package pl.edu.pw.ddm.platform.core.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.primitives.Doubles;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class LocalDatasetProcessor {

    // TODO wrap in wrapper
    private final boolean addIndex;
    private final boolean vectorizeStrings;
    private final Path dataPath;
    private final String separator;
    private final Integer idIndex;
    private final Integer labelIndex;

    private final Map<String, String> labelMapping = new HashMap<>();
    private final Map<String, String> vectorsMapping = new HashMap<>();
    private int vectorsMappingCounter = 0;

    void process() throws IOException {
        if (processed()) {
            return;
        }

        int[] i = new int[]{0};
        Stream<String> stream = Files.lines(dataPath)
                .filter(Predicate.not(String::isBlank));

        if (addIndex) {
            stream = stream.map(l -> (i[0]++) + separator + l);
        }

        if (vectorizeStrings) {
            stream = stream.map(l -> l.split(separator))
                    .map(this::allNumeric)
                    .map(attrs -> String.join(separator, attrs));
        }

        String lines = stream.collect(Collectors.joining(System.lineSeparator()));
        Files.writeString(dataPath, lines);
    }

    private String[] allNumeric(String[] attributes) {
        for (int i = 0; i < attributes.length; ++i) {
            if (i == indexId()) {
                continue;
            } else if (i == indexLabel()) {
                String label = labelMapping.get(attributes[i]);
                if (label == null) {
                    if (DataDescriber.isNumeric(attributes[i])) {
                        // TODO think about this ints conversion if correct
                        label = Optional.of(attributes[i])
                                .map(Doubles::tryParse)
                                .map(Double::intValue)
                                .map(String::valueOf)
                                .orElse(attributes[i]);
                    } else {
                        label = String.valueOf(labelMapping.size());
                    }
                    labelMapping.put(attributes[i], label);
                }
                attributes[i] = label;
            } else if (!DataDescriber.isNumeric(attributes[i])) {
                var value = vectorsMapping.get(attributes[i]);
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
            // FIXME make it 'more correct' in way of comparison int vs null not present here
            return -1;
        }
    }

    private int indexId() {
        if (idIndex != null) {
            return idIndex;
        } else if (addIndex) {
            return 0;
        } else {
            // FIXME should never be here as ID should be always present
            return -1;
        }
    }

    private boolean processed() {
        return !labelMapping.isEmpty();
    }

}
