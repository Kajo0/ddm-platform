package pl.edu.pw.ddm.platform.runner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import scala.Tuple2;
import scala.collection.JavaConverters;
import scala.collection.Seq;
import scala.reflect.ClassTag$;

public final class CentralRunner {

    public static void main(String[] args) throws IOException {
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

        List<Tuple2<Integer, Seq<String>>> tuples = new ArrayList<>(workerAddrs.size());
        for (int i = 0; i < workerAddrs.size(); ++i) {
            tuples.add(new Tuple2(i, workerAddrs.get(i)));
        }

        System.out.println(sc.version());
        scala.collection.mutable.Buffer<Tuple2<Integer, Seq<String>>> seq = JavaConverters.asScalaBuffer(tuples);
        JavaRDD<Integer> rdd = ssc.makeRDD(seq.toSeq(), ClassTag$.MODULE$.apply(Integer.class))
                .toJavaRDD();

        List<String> global = rdd.mapPartitions((FlatMapFunction<Iterator<Integer>, String>) integerIterator -> {
            System.out.println(" LOCAL processing parameters: ");
            integerIterator.forEachRemaining(System.out::println);
            Files.write(Paths.get("/sample.txt"), "Something..".getBytes());
            // TODO run algorithm on local data and return local model
            return new SingletonIterator("Sample result model");
        }, true).collect();

        Files.write(Paths.get("/global.txt"), "Something..".getBytes());
        // TODO run global algorithm
        System.out.println(" GLOBAL processing local models: " + global.size());
        global.forEach(System.out::println);

        // TODO resend/build pipeline

        sc.stop();
    }

}
