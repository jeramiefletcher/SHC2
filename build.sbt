name := "spark-hbase-Connect"

scalaVersion := "2.11.12"

organization := "com.hpe.e2evisibility"

libraryDependencies ++= Seq(

  "org.apache.spark" % "spark-core_2.11" % "2.2.0" % "provided",
  "org.apache.spark" % "spark-catalyst_2.11" % "2.2.0" % "provided",
  "org.apache.phoenix" % "phoenix" % "4.4.0-HBase-0.98" % "provided",
  "org.apache.phoenix" % "phoenix-core" % "4.4.0-HBase-0.98" % "provided",
  "org.apache.spark" % "spark-sql_2.11" % "2.2.0" % "provided",
  "org.apache.hbase" % "hbase-server" % "1.4.10" % "provided",
  "org.apache.hbase" % "hbase" % "1.4.10" % "provided",
  "org.apache.hbase" % "hbase-client" % "1.4.10" % "provided",
  "org.apache.hbase" % "hbase-common" % "1.4.10" % "provided",
  "org.codehaus.jackson" % "jackson-core-asl" % "1.8.8" % "provided",
  "org.slf4j" % "slf4j-log4j12" % "1.7.26",
  "org.apache.avro" % "avro" % "1.8.2",
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"

)

resolvers += Resolver.sonatypeRepo("releases")
resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
resolvers += "Apache HBase" at "https://repository.apache.org/content/repositories/releases"
resolvers += "conjars.org" at "http://conjars.org/repo"


// Configure JAR used with the assembly plug-in
assemblyJarName in assembly := "spark-hbase-connect.jar"

// A special option to exclude Scala itself form our assembly JAR, since Spark
// already bundles Scala.
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

assemblyExcludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
  cp filter {x => x.data.getName.matches("sbt.*") || x.data.getName.matches(".*macros.*")}
}

assemblyMergeStrategy in assembly := {
  case PathList("org", "apache", xs @ _*) => MergeStrategy.last
  case PathList("com", "google", xs @ _*) => MergeStrategy.last
  case PathList("edu", "umd", xs @ _*) => MergeStrategy.last
  case PathList("org", "objectweb", xs @ _*) => MergeStrategy.last
  case PathList("org", "slf4j", xs @ _*) => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}