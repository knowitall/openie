name := "srlie"

organization := "edu.washington.cs.knowitall.openie"

version := "4.0-SNAPSHOT"

crossScalaVersions := Seq("2.10.2", "2.9.3")

scalaVersion <<= crossScalaVersions { (vs: Seq[String]) => vs.head }

resolvers += "Sonatype SNAPSHOTS" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "edu.washington.cs.knowitall.srlie" %% "srlie" % "1.0.0-RC2",
  "edu.washington.cs.knowitall.chunkedextractor" %% "chunkedextractor" % "1.0.5",
  "net.databinder.dispatch" %% "dispatch-core" % "0.10.0", // for remotes
  "com.jsuereth" %% "scala-arm" % "1.3")

scalacOptions ++= Seq("-unchecked", "-deprecation")

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
