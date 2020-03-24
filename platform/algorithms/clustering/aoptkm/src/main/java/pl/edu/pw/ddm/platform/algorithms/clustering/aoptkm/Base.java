package pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.kmeans.Kmeans;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.kmeans.ObjectKmeansCluster;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.dataset.DatasetWriter;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.dataset.ObjectDatasetReader;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.distance.DistanceFunction;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.distance.EuclideanHammingFunction;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.distributed.DistributedCentroidData;
import pl.edu.pw.ddm.platform.algorithms.clustering.aoptkm.utils.point.ObjectPoint;

public abstract class Base {

    protected static final String LOCAL_RESULT_SUFFIX = "_local.txt";
    protected static final String FINAL_RESULT_SUFFIX = "_final.txt";
    protected static final String FINAL_LABELS_RESULT_SUFFIX = "_final_labels.txt";
    protected static final String CENTROIDS_SUFFIX = "_centroids.txt";

    public static void main(String[] args) {
        args = new String[]{
                "aoptkm",
                "/home/mmarkiew/stud/ddm-platform/platform/algorithms/clustering/aoptkm/src/test/resources/config.properties"
        };
        if (args.length < 2) {
            System.out.println(
                    "Required parameters: algorithm:aoptkm, config.properties");
            System.out.println(" or.. Usage: swaplines file1.txt file2.txt rownum linesAmount");
            System.exit(1);
        }
        Base dist = new AutoOpticsKm(args);
        dist.internalRun();
    }

    protected Properties prop = null;
    protected DistanceFunction<Object[]> distanceFunc = new EuclideanHammingFunction();
    protected Integer indexNumber = null;
    protected Integer labelNumber = null;
    protected String inputPath = null;
    protected String outputPath = null;
    protected Double epsilon = null;
    protected Integer groups = null;
    protected Integer iterations = null;
    protected String splitPattern = null;
    protected boolean[] numeric = null;

    protected Double increaseModel = null;
    protected boolean noOneGroup;
    protected boolean minKGroups;
    protected boolean exactKGroups;

    public Base() {
        // do nothing
    }

    public Base(String[] args) {
        String config = args[1];
        Path configPath = Paths.get(config);
        checkState(Files.exists(configPath));

        prop = new Properties();
        try (FileInputStream in = new FileInputStream(configPath.toString())) {
            prop.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String configDir = configPath.getParent().toString();
        inputPath = prop.getProperty("inputPath");
        if (!Paths.get(inputPath).isAbsolute()) {
            inputPath = Paths.get(configDir, inputPath).toString();
        }
        outputPath = prop.getProperty("outputPath");
        if (!Paths.get(outputPath).isAbsolute()) {
            outputPath = Paths.get(configDir, outputPath).toString();
        }
        groups = Integer.valueOf(prop.getProperty("groups"));
        iterations = Integer.valueOf(prop.getProperty("iterations"));
        epsilon = Double.valueOf(prop.getProperty("epsilon"));
        indexNumber = Ints.tryParse(prop.getProperty("indexNumber"));
        labelNumber = Ints.tryParse(prop.getProperty("labelNumber"));
        splitPattern = prop.getProperty("splitPattern", ObjectDatasetReader.SPLIT_PATTERN);

        String categorical = prop.getProperty("numeric", null);
        if (!Strings.isNullOrEmpty(categorical)) {
            List<Boolean> cats = Stream.of(categorical.split(","))
                    .map(Boolean::valueOf)
                    .collect(Collectors.toList());

            numeric = new boolean[cats.size()];
            for (int i = 0; i < cats.size(); ++i) {
                numeric[i] = cats.get(i);
            }
        }

        checkNotNull(outputPath);
        checkState(Files.exists(Paths.get(inputPath)));

        try {
            Files.createDirectories(Paths.get(outputPath).getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String incModel = prop.getProperty("increaseModel");
        if (incModel != null) {
            increaseModel = Double.parseDouble(incModel);
            if (increaseModel <= 0 || increaseModel > 1) {
                increaseModel = null;
            }
        }

        noOneGroup = Boolean.parseBoolean(prop.getProperty("noOneGroup"));
        minKGroups = Boolean.parseBoolean(prop.getProperty("minKGroups"));
        exactKGroups = Boolean.parseBoolean(prop.getProperty("exactKGroups"));
    }

    public void internalRun() {
        Stopwatch timer = Stopwatch.createStarted();

        run();

        long elapsed = timer.stop().elapsed(TimeUnit.MILLISECONDS);
        if (System.getenv("LOG_TIME_DISTRIBUTED_ALL") != null) {
            System.out.println("    All Distributed Time elapsed: " + elapsed + " ms");
        }
    }

    public abstract void run();

    protected List<ObjectPoint> getRandomAdditionalPoints(List<ObjectPoint> points) {
        List<ObjectPoint> additional = Lists.newArrayList();
        if (increaseModel != null) {
            List<Integer> shuffl = Lists.newArrayList();
            for (int i = 0; i < points.size(); ++i) {
                shuffl.add(i);
            }
            Collections.shuffle(shuffl);
            for (int i = 0; i < increaseModel * points.size(); ++i) {
                ObjectPoint p = points.get(shuffl.get(i));
                additional.add(new ObjectPoint(p.values));
            }
        }
        return additional;
    }

    protected List<ObjectPoint> getOrderedAdditionalPoints(List<ObjectKmeansCluster> calcClusters) {
        List<ObjectPoint> additional = Lists.newArrayList();
        if (increaseModel != null) {
            calcClusters.forEach(c -> {
                List<ObjectPoint> points = c.cluster;
                List<ObjectPoint> sorted = points.stream()
                        .sorted((o1, o2) -> Double.compare(c.centroid.distance(o1, distanceFunc),
                                c.centroid.distance(o2, distanceFunc)))
                        .collect(Collectors.toList());
                for (int i = 0; i < increaseModel * points.size(); ++i) {
                    ObjectPoint p = sorted.get(i);
                    additional.add(new ObjectPoint(p.values));
                }
            });
        }
        return additional;
    }

    protected List<DistributedCentroidData> getRandomAdditionalClusterPoints(ObjectKmeansCluster cluster,
                                                                             DistributedCentroidData data) {
        List<DistributedCentroidData> additional = Lists.newArrayList();
        if (increaseModel != null) {
            List<ObjectPoint> points = cluster.cluster;
            List<Integer> shuffl = Lists.newArrayList();
            for (int i = 0; i < points.size(); ++i) {
                shuffl.add(i);
            }
            Collections.shuffle(shuffl);
            for (int i = 0; i < increaseModel * points.size(); ++i) {
                DistributedCentroidData d = new DistributedCentroidData();
                ObjectPoint p = points.get(shuffl.get(i));
                d.setCentroid(Arrays.copyOf(p.values, p.values.length));
                d.setM(data.getM());
                d.setMax(data.getMax());
                d.setMin(data.getMin());
                d.setS(data.getS());
                d.setSD(data.getSD());
                d.setSS(data.getSS());
                d.setValue(data.getValue());
                d.setN(data.getN());
                additional.add(d);
            }
        }
        return additional;
    }

    protected List<DistributedCentroidData> getOrderedAdditionalClusterPoints(ObjectKmeansCluster cluster,
                                                                              DistributedCentroidData data) {
        List<DistributedCentroidData> additional = Lists.newArrayList();
        if (increaseModel != null) {
            List<ObjectPoint> points = cluster.cluster;
            List<ObjectPoint> sorted = points.stream()
                    .sorted((o1, o2) -> Double.compare(cluster.centroid.distance(o1, distanceFunc),
                            cluster.centroid.distance(o2, distanceFunc)))
                    .collect(Collectors.toList());

            for (int i = 0; i < increaseModel * points.size(); ++i) {
                DistributedCentroidData d = new DistributedCentroidData();
                ObjectPoint p = sorted.get(i);
                d.setCentroid(Arrays.copyOf(p.values, p.values.length));
                d.setM(data.getM());
                d.setMax(data.getMax());
                d.setMin(data.getMin());
                d.setS(data.getS());
                d.setSD(data.getSD());
                d.setSS(data.getSS());
                d.setValue(data.getValue());
                d.setN(data.getN());
                additional.add(d);
            }
        }
        return additional;
    }


    @Deprecated
    protected ObjectPoint[] findMinMax(Map<String, List<ObjectPoint>> nodes) {
        return null;
//        List<ObjectPoint[]> localMinMax = Lists.newArrayList();
//        localMinMax.addAll(nodes.entrySet()
//                .stream()
//                .map(entry -> NdkmUtils.findLocalMinMax(entry.getValue()))
//                .collect(Collectors.toList()));
//        return NdkmUtils.findGlobalMinMax(localMinMax);
    }

    protected Map<String, List<ObjectPoint>> readNodes() {
        Map<String, List<ObjectPoint>> nodes = Maps.newHashMap();
        Path pathIn = Paths.get(inputPath);
        try (Stream<String> stream = Files.lines(pathIn)) {
            stream.forEach(line -> {
                Path path = Paths.get(line);
                checkState(Files.exists(path));
                nodes.put(path.getFileName().toString(),
                        ObjectDatasetReader.read(path.toString(), numeric, indexNumber, labelNumber, splitPattern));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nodes;
    }

    protected Map<String, List<ObjectPoint>> normalizeNodes(Map<String, List<ObjectPoint>> nodes) {
        // TODO normalization removed
        return nodes;
//        Map<String, List<ObjectPoint>> normalizedNodes = Maps.newHashMap();
//        ObjectPoint[] minMax = findMinMax(nodes);
//
//        nodes.entrySet().forEach(entry -> {
//            List<ObjectPoint> nPts = NdkmUtils.normalizeMinMax(entry.getValue(), minMax[0], minMax[1]);
//            normalizedNodes.put(entry.getKey(), nPts);
//        });
//
//        return normalizedNodes;
    }

    protected List<ObjectKmeansCluster> runKmeans(List<ObjectPoint> points, List<ObjectPoint> centroids) {
        Kmeans kmeans = new Kmeans(epsilon, centroids != null ? centroids.size() : groups, iterations, points);
        kmeans.kmeans(centroids);
        return kmeans.getClusters();
    }

    protected List<ObjectPoint> prepareRecalcCentroids(List<ObjectKmeansCluster> globalClusters) {
        List<ObjectPoint> recalcCentroids = Lists.newArrayList();
        globalClusters.forEach(c -> recalcCentroids.add(c.centroid));
        for (int i = 0; i < recalcCentroids.size(); ++i) {
            recalcCentroids.get(i).clusterId = i;
        }
        return recalcCentroids;
    }


    protected void writePreLocalResult(String nodeName, List<ObjectKmeansCluster> calcClusters) {
        //writeResult(calcClusters, outputPath + "_" + nodeName + LOCAL_RESULT_SUFFIX);
    }

    protected void writeLocalResults(Map<String, List<ObjectPoint>> nodes, List<ObjectPoint> recalcCentroids) {
        nodes.entrySet().stream().forEach(entry -> {
            List<ObjectKmeansCluster> rClusters = Kmeans.prepareCentroidClusters(recalcCentroids);
            startLog("common local update");
            Kmeans.assignPoints(entry.getValue(), rClusters, distanceFunc);
            stopLog(null);
            //writeResult(rClusters, outputPath + "_" + entry.getKey());
        });
    }

    protected void writeGlobalResult(List<ObjectPoint> allPoints, List<ObjectPoint> recalcCentroids) {
        writeGlobalResult(allPoints, recalcCentroids, false);
    }

    protected void writeGlobalResult(List<ObjectPoint> allPoints, List<ObjectPoint> recalcCentroids,
                                     boolean filterEmpty) {
        List<ObjectKmeansCluster> recalcClusters = Kmeans.prepareCentroidClusters(recalcCentroids);
        Kmeans.assignPoints(allPoints, recalcClusters, distanceFunc);
        if (filterEmpty) {
            recalcClusters = recalcClusters.stream().filter(c -> !c.cluster.isEmpty()).collect(Collectors.toList());
            System.out.println("After filter empty groups: " + recalcClusters.size());
        }
        //writeResult(recalcClusters, outputPath + FINAL_RESULT_SUFFIX);
        writeLabels(recalcClusters, outputPath + FINAL_LABELS_RESULT_SUFFIX);
    }

    protected void writeCentroids(List<ObjectPoint> recalcCentroids) {
        Map<Integer, List<ObjectPoint>> map = Maps.newHashMap();
        recalcCentroids.forEach(point -> map.put(map.size(), Lists.newArrayList(point)));
        DatasetWriter.write(outputPath + CENTROIDS_SUFFIX, map);
    }


    protected void writeResult(List<ObjectKmeansCluster> clusters, String path) {
        Map<Integer, List<ObjectPoint>> map = Maps.newHashMap();
        clusters.forEach(cluster -> map.put(map.size(), cluster.cluster));
        DatasetWriter.write(path, map);
    }

    protected void writeLabels(List<ObjectKmeansCluster> clusters, String path) {
        StringBuilder str = new StringBuilder();
        List<ObjectPoint> points = Lists.newArrayList();
        clusters.forEach(c -> points.addAll(c.cluster));

        points.sort((p1, p2) -> p1.index.compareTo(p2.index));
        points.forEach(p -> str.append(p.clusterId + "\n"));
        try {
            Path p = Paths.get(path);
            Files.write(p, str.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Stopwatch timer;

    protected void startLog(String msg) {
        if (System.getenv("LOG_TIME_DISTRIBUTED_INTERNAL") == null) {
            return;
        }
        if (msg != null) {
            System.out.println("      Start log: " + msg);
        }
        if (timer != null && timer.isRunning()) {
            throw new RuntimeException("Timer not stopped!");
        }

        timer = Stopwatch.createStarted();
    }

    protected void stopLog(String msg) {
        if (System.getenv("LOG_TIME_DISTRIBUTED_INTERNAL") == null) {
            return;
        }
        if (msg != null) {
            System.out.println("      Stop log: " + msg);
        }
        if (timer == null) {
            throw new RuntimeException("Timer not started!");
        }

        long elapsed = timer.stop().elapsed(TimeUnit.MILLISECONDS);
        System.out.println("        Distributed internal Time elapsed: " + elapsed + " ms");
    }

}
