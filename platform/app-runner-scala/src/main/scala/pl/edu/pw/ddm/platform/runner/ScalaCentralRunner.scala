package pl.edu.pw.ddm.platform.runner

import java.net.InetAddress

import org.apache.spark.SparkContext
import org.reflections.Reflections
import pl.edu.pw.ddm.platform.interfaces.algorithm.LocalProcessor
import pl.edu.pw.ddm.platform.interfaces.mining.MiningMethod
import pl.edu.pw.ddm.platform.interfaces.model.{GlobalModel, LocalModel}

import scala.collection.{JavaConversions, Seq}

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

    // FIXME try to force to mek it work
    //    val tuples = ArrayBuffer.empty[(Int, Seq[String])]
    //    for ((n, i) <- workerAddrs.zipWithIndex) {
    //        val t = Tuple2(i, List(n))
    //        tuples += t
    //        println(t)
    //    }

    // FIXME workaround: somehow it forces to use all workers
    val tuples = Range.inclusive(0, workerAddrs.size - 1)
    sc.parallelize(tuples, tuples.size)
      .map(_ => { InetAddress.getLocalHost })
      .collect()
      .foreach { println }

    //    val rdd = sc.makeRDD(tuples)
    //    val global = rdd.mapPartitions(iterator => {
    val global = sc.parallelize(tuples, tuples.size)
      .mapPartitions(iterator => {
        println(" LOCAL processing parameters: ")
        //      Files.write(Paths.get("/sample.txt"), "Something..".getBytes)
        // TODO run algorithm on local data and return local model
        Iterator("Sample result model from " + InetAddress.getLocalHost + " with data " + iterator.mkString(","))
      }).collect

    //    Files.write(Paths.get("/global.txt"), "Something..".getBytes)
    // TODO run global algorithm
    println(" GLOBAL processing local models: " + global.length)
    global.foreach { println }
    val globalModel = "GLOBAL_MODEL!"

    // TODO resend/build pipeline
    val updatingRdd = sc.parallelize(Seq.fill(workerAddrs.length) { globalModel }, workerAddrs.length)
      .mapPartitions(iterator => {
        println(" UPDATING LOCAL processing parameters: ")
        //      Files.write(Paths.get("/sample.txt"), "Something..".getBytes)
        // TODO run algorithm on local data and return local model
        Iterator("UPDATE ACK from " + InetAddress.getLocalHost + " with data " + iterator.mkString(","))
      }).collect
    println(" LOCAL UPDATED models: " + updatingRdd.length)
    updatingRdd.foreach { println }

    sc.stop()
  }

  def runAlg(): Unit = {
    println("---------------------------------")
    val reflections = new Reflections("pl.edu.pw.ddm.platform")
    val classes = reflections.getSubTypesOf(classOf[LocalProcessor[_ <: LocalModel, _ <: GlobalModel, _ <: MiningMethod]])
    JavaConversions.asScalaSet(classes)
      .foreach {
        println
      }
    val clazz = classes.stream()
      .findFirst()
      .get()
    val ctor = clazz.getDeclaredConstructor()
      .newInstance()
    ctor.processLocal(null, null)
    println("---------------------------------")
  }

}
