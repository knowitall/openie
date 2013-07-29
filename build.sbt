name := "srlie"

organization := "edu.washington.cs.knowitall.openie"

version := "4.0-SNAPSHOT"

crossScalaVersions := Seq("2.10.2", "2.9.3")

scalaVersion <<= crossScalaVersions { (vs: Seq[String]) => vs.head }

resolvers += "Sonatype SNAPSHOTS" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  // extractor components
  "edu.washington.cs.knowitall.srlie" %% "srlie" % "1.0.0-RC2",
  "edu.washington.cs.knowitall.chunkedextractor" %% "chunkedextractor" % "1.0.5",
  // for splitting sentences
  "edu.washington.cs.knowitall.nlptools" % "nlptools-sentence-opennlp_2.10" % "2.4.2",
  // for remote components
  "net.databinder.dispatch" %% "dispatch-core" % "0.10.0",
  // resource management
  "com.jsuereth" %% "scala-arm" % "1.3",
  // logging
  "org.slf4j" % "slf4j-api" % "1.7.2",
  "ch.qos.logback" % "logback-classic" % "1.0.9")

scalacOptions ++= Seq("-unchecked", "-deprecation")

// custom options for high memory usage

javaOptions += "-Xmx4G"

javaOptions += "-XX:+UseConcMarkSweepGC"

fork in run := true

connectInput in run := true // forward stdin/out to fork

licenses := Seq("Ollie Software License Agreement" -> url("https://raw.github.com/knowitall/ollie/master/LICENSE"))

homepage := Some(url("https://github.com/knowitall/openie"))

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}
