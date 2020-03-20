package pl.edu.pw.ddm.platform.runner;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import pl.edu.pw.ddm.platform.interfaces.model.GlobalModel;

public final class CentralRunner {

    private final String masterAddr;
    private final List<String> workerAddrs;
    private final String algorithmId;
    private final String dataId;

    private final JavaSparkContext sc;
    private final List<Integer> nodeStubList;

    private List<ModelWrapper> localModels;
    private ModelWrapper globalModel;
    private List<ModelWrapper> updatedAcks;

    public CentralRunner(String masterAddr, List<String> workerAddrs, String algorithmId, String dataId) {
        this.masterAddr = masterAddr;
        this.workerAddrs = workerAddrs;
        this.algorithmId = algorithmId;
        this.dataId = dataId;

        SparkContext ssc = SparkContext.getOrCreate();
        this.sc = JavaSparkContext.fromSparkContext(ssc);

        this.nodeStubList = IntStream.range(0, workerAddrs.size())
                .boxed()
                .collect(Collectors.toList());

        this.localModels = new ArrayList<>(nodeStubList.size());
    }

    public static void main(String[] args) {
        // TODO args with num dataId and maybe start parameters
        if (args.length < 4) {
            System.err.println("No args provided. [masterAddr, workerAddrs, algorithmId, dataId]");
            System.exit(1);
        }
        String masterAddr = args[0];
        List<String> workerAddrs = Arrays.asList(args[1].split(","));
        String algorithmId = args[2];
        String dataId = args[3];

        new CentralRunner(masterAddr, workerAddrs, algorithmId, dataId)
                .run();
    }

//    private static void runAlg() throws Exception {
//        Reflections reflections = new Reflections("pl.edu.pw.ddm.platform");
//        Set<Class<? extends LocalProcessor>> classes = reflections.getSubTypesOf(LocalProcessor.class);
//        Class<? extends LocalProcessor> clazz = classes.stream()
//                .peek(System.out::println)
//                .findFirst()
//                .get();
//        LocalProcessor a = clazz.getDeclaredConstructor()
//                .newInstance();
//        a.processLocal(null, null);
//    }

    private void run() {
        // TODO send clear ID to every agent
        performEachNodeDistributionWorkaround();

        processLocal();
        processGlobal();
        updateLocal();

        new CentralDdmSummary(localModels, globalModel, updatedAcks, masterAddr, workerAddrs)
                .printModelsSummary()
                .printDispersionSummary();

        sc.stop();
    }

    private void performEachNodeDistributionWorkaround() {
        sc.parallelize(nodeStubList, nodeStubList.size())
                .map(n -> InetAddress.getLocalHost())
                .collect()
                .forEach(System.out::println);
    }

    private void processLocal() {
        // FIXME not using preferred locations
        localModels = sc.parallelize(nodeStubList, nodeStubList.size())
                .mapPartitions(iterator -> {
                    Integer id = iterator.next();
                    PersistentIdStamper.save(id);
                    StringLocalModel model = new StringLocalModel("time=" + System.currentTimeMillis());
                    ModelWrapper wrapper = ModelWrapper.local(model, InetAddress.getLocalHost().toString(), id);
                    return new SingletonIterator(wrapper);
                }).collect();
    }

    private void processGlobal() {
        globalModel = Optional.of("GLOBAL_MODEL! time=" + System.currentTimeMillis())
                .map(StringGlobalModel::new)
                .map(ModelWrapper::global)
                .get();
    }

    private void updateLocal() {
        List<GlobalModel> globals = Collections.nCopies(workerAddrs.size(), globalModel.getGlobalModel());
        updatedAcks = sc.parallelize(globals, workerAddrs.size())
                .mapPartitions(iterator -> {
                    Integer id = PersistentIdStamper.read();
                    StringLocalModel model = new StringLocalModel("time=" + System.currentTimeMillis());
                    ModelWrapper wrapper = ModelWrapper.local(model, InetAddress.getLocalHost().toString(), id);
                    return new SingletonIterator(wrapper);
                }).collect();
    }

}
