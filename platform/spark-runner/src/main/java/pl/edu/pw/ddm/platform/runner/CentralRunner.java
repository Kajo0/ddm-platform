package pl.edu.pw.ddm.platform.runner;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;

public final class CentralRunner {

    public static void main(String[] args) {
        // TODO args with num dataId and maybe start parameters
        if (args.length < 1) {
            System.err.println("No args provided.");
            System.exit(1);
        }
        int numOfSlices = Integer.parseInt(args[0]);

        SparkContext ssc = SparkContext.getOrCreate();
        JavaSparkContext sc = JavaSparkContext.fromSparkContext(ssc);

        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6);
        JavaRDD<Integer> rdd = sc.parallelize(list, numOfSlices);

        List<String> global = rdd.mapPartitions((FlatMapFunction<Iterator<Integer>, String>) integerIterator -> {
            System.out.println(" LOCAL processing parameters: ");
            integerIterator.forEachRemaining(System.out::println);
            // TODO run algorithm on local data and return local model
            return new SingletonIterator("Sample result model");
        }).collect();

        // TODO run global algorithm
        System.out.println(" GLOBAL processing local models: " + global.size());
        global.forEach(System.out::println);

        // TODO resend/build pipeline

        sc.stop();
    }

}
