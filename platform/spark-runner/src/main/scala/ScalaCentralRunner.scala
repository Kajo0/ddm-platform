
import org.apache.spark.SparkContext

import scala.collection.Seq
import scala.collection.mutable.ArrayBuffer

object ScalaCentralRunner {

  //  @throws[IOException]
  def main(args: Array[String]): Unit = {
    if (args.length < 4) {
      System.err.println("No args provided. [masterAddr, workerAddrs, algorithmId, dataId]")
      System.exit(1)
    }
    val masterAddr = args(0)
    val workerAddrs = args(1).split(",")
    val algorithmId = args(2)
    val dataId = args(3)

    val sc = SparkContext.getOrCreate

    val tuples = ArrayBuffer.empty[(Int, Seq[String])]
    for ((n, i) <- workerAddrs.zipWithIndex) {
      tuples += Tuple2(i, List(n))
    }

    val rdd = sc.makeRDD(tuples)
    val global = rdd.mapPartitions(iterator => {
      println(" LOCAL processing parameters: ")
      iterator.foreach {
        println
      }
      //      Files.write(Paths.get("/sample.txt"), "Something..".getBytes)
      // TODO run algorithm on local data and return local model
      Iterator("Sample result model")
    }).collect

    //    Files.write(Paths.get("/global.txt"), "Something..".getBytes)
    // TODO run global algorithm
    println(" GLOBAL processing local models: " + global.length)
    global.foreach {
      println
    }

    // TODO resend/build pipeline
    sc.stop()
  }
}
