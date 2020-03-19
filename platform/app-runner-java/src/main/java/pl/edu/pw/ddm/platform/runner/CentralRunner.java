package pl.edu.pw.ddm.platform.runner;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.reflections.Reflections;
import pl.edu.pw.ddm.platform.interfaces.algorithm.LocalProcessor;

public final class CentralRunner {

    public static void main(String[] args) throws Exception {
        // TODO args with num dataId and maybe start parameters
        if (args.length < 4) {
            System.err.println("No args provided. [masterAddr, workerAddrs, algorithmId, dataId]");
            System.exit(1);
        }
        String masterAddr = args[0];
        List<String> workerAddrs = Arrays.asList(args[1].split(","));
        String algorithmId = args[2];
        String dataId = args[3];

        SparkContext ssc = SparkContext.getOrCreate();
        JavaSparkContext sc = JavaSparkContext.fromSparkContext(ssc);

//        List<Tuple2<Integer, Seq<String>>> tuples = new ArrayList<>(workerAddrs.size());
//        for (int i = 0; i < workerAddrs.size(); ++i) {
//            List<String> list = Collections.singletonList(workerAddrs.get(i));
//            tuples.add(new Tuple2(i, JavaConverters.asScalaBufferConverter(list).asScala()));
//        }
        List<Integer> tuples = IntStream.range(0, workerAddrs.size())
                .boxed()
                .collect(Collectors.toList());
        sc.parallelize(tuples, tuples.size())
                .map(n -> InetAddress.getLocalHost())
                .collect()
                .forEach(System.out::println);

//        scala.collection.mutable.Buffer<Tuple2<Integer, Seq<String>>> seq = JavaConverters.asScalaBufferConverter(tuples).asScala();
//        JavaRDD<Integer> rdd = ssc.makeRDD(seq.toSeq(), ClassTag$.MODULE$.apply(Integer.class))
//                .toJavaRDD();

        // FIXME not using preferred locations
        List<String> global = sc.parallelize(tuples, tuples.size())
                .mapPartitions(iterator -> {
                    System.out.println(" LOCAL processing parameters: ");
//            Files.write(Paths.get("/sample.txt"), "Something..".getBytes());
                    // TODO run algorithm on local data and return local model
                    return new SingletonIterator("Sample result model from " + InetAddress.getLocalHost() + " with data " + iterator.next() + " (" + iterator.hasNext() + ")");
                }).collect();

//        Files.write(Paths.get("/global.txt"), "Something..".getBytes());
        // TODO run global algorithm
        System.out.println(" GLOBAL processing local models: " + global.size());
        global.forEach(System.out::println);
        String globalModel = "GLOBAL_MODEL!";

        // TODO resend/build pipeline
        List<String> globals = Collections.nCopies(workerAddrs.size(), globalModel);
        List<String> updatingRdd = sc.parallelize(globals, workerAddrs.size())
                .mapPartitions(iterator -> {
                    System.out.println(" UPDATING LOCAL processing parameters: ");
                    //      Files.write(Paths.get("/sample.txt"), "Something..".getBytes)
                    // TODO run algorithm on local data and return local model
                    return new SingletonIterator("UPDATE ACK from " + InetAddress.getLocalHost() + " with data " + iterator.next() + " (" + iterator.hasNext() + ")");
                }).collect();
        System.out.println(" LOCAL UPDATED models: " + updatingRdd.size());
        updatingRdd.forEach(System.out::println);

        runAlg();

        sc.stop();
    }

    private static void runAlg() throws Exception {
        Reflections reflections = new Reflections("pl.edu.pw.ddm.platform");
        Set<Class<? extends LocalProcessor>> classes = reflections.getSubTypesOf(LocalProcessor.class);
        Class<? extends LocalProcessor> clazz = classes.stream()
                .peek(System.out::println)
                .findFirst()
                .get();
        LocalProcessor a = clazz.getDeclaredConstructor()
                .newInstance();
        a.processLocal(null, null);
    }

}
