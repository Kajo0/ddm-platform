name := "app-runner-scala"

version := "0.1"

scalaVersion := "2.11.12"
javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.5"
