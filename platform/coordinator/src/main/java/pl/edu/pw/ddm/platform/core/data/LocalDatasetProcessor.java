package pl.edu.pw.ddm.platform.core.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.primitives.Doubles;
import de.lmu.ifi.dbs.elki.data.synthetic.bymodel.GeneratorSingleCluster;
import de.lmu.ifi.dbs.elki.math.statistics.distribution.NormalDistribution;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

// TODO extract interface and name CsvDatasetProcessor
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
    private final AtomicInteger vectorsMappingCounter = new AtomicInteger(0);
    private final AtomicInteger indexGen = new AtomicInteger(0);
    private final DataStatisticsCalculator statistics = new DataStatisticsCalculator();

    void process() throws IOException {
        if (processed()) {
            return;
        }

        Stream<String> stream = Files.lines(dataPath)
                .filter(Predicate.not(String::isBlank));

        if (addIndex) {
            stream = stream.map(l -> (indexGen.getAndIncrement()) + separator + l);
        }

        if (vectorizeStrings) {
            stream = stream.map(l -> l.split(separator))
                    .map(this::allNumeric)
                    .map(attrs -> String.join(separator, attrs));
        }

        String lines = stream.collect(Collectors.joining(System.lineSeparator()));
        Files.writeString(dataPath, lines);

        // FIXME optimize somehow during processing blank lines?
        if (!addIndex) {
            indexGen.setPlain((int) Files.lines(dataPath)
                    .count());
        }
    }

    // TODO extract to CsvExpander or sth similar
    void expand(int amount, Long seed) throws IOException {
        Files.lines(dataPath)
                .map(this::readRow)
                .forEach(la -> statistics.countSum(la.label, la.attrs));
        statistics.mean();
        Files.lines(dataPath)
                .map(this::readRow)
                .forEach(la -> statistics.stddevSum(la.label, la.attrs));
        statistics.stddev();

        var rand = Optional.ofNullable(seed)
                .map(Random::new)
                .orElseGet(Random::new);
        var some = statistics.getClassMeanMap()
                .values()
                .iterator()
                .next();
        var labels = statistics.getClassMeanMap()
                .keySet();
        labels.forEach(label -> {
            GeneratorSingleCluster gsc = new GeneratorSingleCluster("dummy", amount, 1, rand);
            var mean = statistics.getClassMeanMap()
                    .get(label);
            var stddev = statistics.getClassStdDevMap()
                    .get(label);
            for (int i = 0; i < some.length; ++i) {
                gsc.addGenerator(new NormalDistribution(mean[i], stddev[i], rand));
            }

            gsc.generate(amount)
                    .stream()
                    .map(data -> prepareRow(data, label))
                    .map(attrs -> String.join(separator, attrs))
                    .forEach(line -> {
                        try {
                            Files.writeString(dataPath, System.lineSeparator() + line, StandardOpenOption.APPEND);
                        } catch (IOException e) {
                            // FIXME stack trace should not be here
                            e.printStackTrace();
                        }
                    });
        });
    }

    private String[] prepareRow(double[] data, String label) {
        var len = data.length + (indexLabel() != -1 ? 1 : 0) + (indexId() != -1 ? 1 : 0);
        var strAttrs = new String[len];
        for (int i = 0; i < len; ++i) {
            if (i == indexId()) {
                // FIXME when real count will be exist then remove negation
                // FIXME overlapping ids possible --^^ up
                strAttrs[i] = (addIndex ? "" : "-") + indexGen.getAndIncrement();
            } else if (i == indexLabel()) {
                strAttrs[i] = String.valueOf(label);
            } else {
                var index = i - (i < indexId() ? 0 : 1) - (i < indexLabel() ? 0 : 1);
                strAttrs[i] = String.valueOf(data[index]);
            }
        }
        return strAttrs;
    }

    private LabelToNumericAttr readRow(String line) {
        var strAttrs = line.split(separator);
        var attrs = new double[strAttrs.length - (indexLabel() != -1 ? 1 : 0) - (indexId() != -1 ? 1 : 0)];
        for (int i = 0; i < strAttrs.length; ++i) {
            if (i == indexId() || i == indexLabel()) {
                continue;
            }
            var index = i - (i < indexId() ? 0 : 1) - (i < indexLabel() ? 0 : 1);
            attrs[index] = Double.parseDouble(strAttrs[i]);
        }

        return new LabelToNumericAttr(strAttrs[indexLabel()], attrs);
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
                    value = String.valueOf(vectorsMappingCounter.getAndIncrement());
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
        return !labelMapping.isEmpty() || indexGen.get() > 0;
    }

    @Value
    private static class LabelToNumericAttr {

        String label;
        double[] attrs;
    }

}
