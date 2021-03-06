import sbt._
import Keys._
import Process._
import java.io.File



object ScalaMeterBuild extends Build {

  val publishUser = "SONATYPE_USER"
  val publishPass = "SONATYPE_PASS"

  val scalaMeterVersion = "0.6-SNAPSHOT"

  val userPass = for {
    user <- sys.env.get(publishUser)
    pass <- sys.env.get(publishPass)
  } yield (user, pass)

  val publishCreds: Seq[Setting[_]] = Seq(userPass match {
    case Some((user, pass)) =>
      credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, pass)
    case None =>
      // prevent publishing
      publish <<= streams.map(_.log.info("Publishing to Sonatype is disabled since the \"" + publishUser + "\" and/or \"" + publishPass + "\" environment variables are not set."))
  })

  val scalaMeterSettings = Defaults.defaultSettings ++ publishCreds ++ Seq(
    name := "scalameter",
    organization := "com.storm-enroute",
    version := scalaMeterVersion,
    scalaVersion := "2.11.1",
    crossScalaVersions := Seq("2.10.4", "2.11.1"),
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-Xlint"),
    libraryDependencies <++= (scalaVersion)(sv => dependencies(sv)),
    resolvers ++= Seq(
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"
    ),
    publishMavenStyle := true,
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    pomExtra :=
      <url>http://scalameter.github.io/</url>
      <licenses>
        <license>
          <name>BSD-style</name>
          <url>http://opensource.org/licenses/BSD-3-Clause</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:scalameter/scalameter.git</url>
        <connection>scm:git:git@github.com:scalameter/scalameter.git</connection>
      </scm>
      <developers>
        <developer>
          <id>axel22</id>
          <name>Aleksandar Prokopec</name>
          <url>http://axel22.github.com/</url>
        </developer>
      </developers>
  )

  def dependencies(scalaVersion: String) = CrossVersion.partialVersion(scalaVersion) match {
    case Some((2,11)) => List (
      "org.scalatest" %% "scalatest" % "2.1.3" % "test",
      "com.github.wookietreiber" %% "scala-chart" % "0.4.2",
      "org.apache.commons" % "commons-math3" % "3.2",
      "org.scala-tools.testing" % "test-interface" % "0.5",
      "org.scala-lang" % "scala-reflect" % "2.11.0",
      "org.scala-lang.modules" %% "scala-xml" % "1.0.1",
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1"
      )
    case Some((2,10)) => List (
      "org.scalatest" %% "scalatest" % "2.1.2" % "test",
      "com.github.wookietreiber" %% "scala-chart" % "0.4.0",
      "org.apache.commons" % "commons-math3" % "3.2",
      "org.scala-tools.testing" % "test-interface" % "0.5"
      )
    case _ => Nil
  }

  val scalaMeterCoreSettings = Defaults.defaultSettings ++ publishCreds ++ Seq(
    name := "scalameter-core",
    organization := "com.storm-enroute",
    version := scalaMeterVersion,
    scalaVersion := "2.11.1",
    crossScalaVersions := Seq("2.10.4", "2.11.1"),
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-Xlint"),
    libraryDependencies <++= (scalaVersion)(sv => coreDependencies(sv)),
    resolvers ++= Seq(
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"
    ),
    publishMavenStyle := true,
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    pomExtra :=
      <url>http://scalameter.github.io/</url>
      <licenses>
        <license>
          <name>BSD-style</name>
          <url>http://opensource.org/licenses/BSD-3-Clause</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:scalameter/scalameter.git</url>
        <connection>scm:git:git@github.com:scalameter/scalameter.git</connection>
      </scm>
      <developers>
        <developer>
          <id>axel22</id>
          <name>Aleksandar Prokopec</name>
          <url>http://axel22.github.com/</url>
        </developer>
      </developers>
  )

  def coreDependencies(scalaVersion: String) = CrossVersion.partialVersion(scalaVersion) match {
    case Some((2,11)) => List (
      "org.apache.commons" % "commons-math3" % "3.2",
      "org.scala-lang" % "scala-reflect" % "2.11.0",
      "org.scala-lang.modules" %% "scala-xml" % "1.0.1",
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1"
      )
    case Some((2,10)) => List (
      "org.apache.commons" % "commons-math3" % "3.2"
      )
    case _ => Nil
  }

  val javaCommand = TaskKey[String](
    "java-command",
    "Creates a java vm command for launching a process."
  )

  val javaCommandSetting = javaCommand <<= (
    dependencyClasspath in Compile,
    artifactPath in (Compile, packageBin),
    artifactPath in (Test, packageBin),
    packageBin in Compile,
    packageBin in Test
  ) map {
    (dp, jar, testjar, pbc, pbt) => // -XX:+UseConcMarkSweepGC  -XX:-DoEscapeAnalysis -XX:MaxTenuringThreshold=12 -XX:+PrintGCDetails 
    //val cp = dp.map("\"" + _.data + "\"") :+ ("\"" + jar +"\"") :+ ("\"" + testjar + "\"")
    val cp = dp.map(_.data) :+ jar :+ testjar
    val javacommand = "java -Xmx2048m -Xms2048m -XX:+UseCondCardMark -verbose:gc -server -cp %s".format(
      cp.mkString(File.pathSeparator)
    )
    javacommand
  }
  
  val runsuiteTask = InputKey[Unit](
    "runsuite",
    "Runs the benchmarking suite."
  ) <<= inputTask {
    (argTask: TaskKey[Seq[String]]) =>
    (argTask, javaCommand) map {
      (args, jc) =>
      val javacommand = jc
      val comm = javacommand + " " + "org.scalameter.Main" + " " + args.mkString(" ")
      streams.map(_.log.info("Executing: " + comm))
      import sys.process._
      comm !
    }
  }

  /* projects */

  lazy val scalaMeterCore = Project(
    "scalameter-core",
    file("scalameter-core"),
    settings = scalaMeterCoreSettings
  )

  lazy val scalaMeter = Project(
    "scalameter",
    file("."),
    settings = scalaMeterSettings ++ Seq(javaCommandSetting, runsuiteTask)
  ) dependsOn (
    scalaMeterCore
  )

}
