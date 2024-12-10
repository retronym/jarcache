Global / crossPaths := false

Global / version := "0.1.0-SNAPSHOT"

Global / organization := "org.gitbub.retronym.jarcache"
Global / autoScalaLibrary := false

val agent = project.settings(
  crossPaths := false,
  libraryDependencies += "net.bytebuddy" % "byte-buddy" % "1.15.10",
  Compile/unmanagedResources/excludeFilter := "MANIFEST.MF",
  Compile/packageOptions += Package.ManifestAttributes(
    "Premain-Class" -> "io.github.retronym.jarcache.Agent",
    "Can-Redefine-Classes" -> "true",
    "Can-Retransform-Classes" -> "true",
    "Can-Set-Native-Method-Prefix" -> "true"
  )
)

val demo = project.dependsOn(agent)
  .settings(
    Compile/javaOptions += s"-javaagent:${(agent / Compile / assembly).value.getAbsolutePath}",
    javacOptions ++= Seq(
      "--add-modules", "jdk.compiler"
    ),
    javaOptions ++= javacOptions.value,
    libraryDependencies += "org.moditect.jfrunit" % "jfrunit-core" % "1.0.0.Alpha2",
    libraryDependencies += "org.junit.jupiter" % "junit-jupiter-api" % "5.7.0",
    libraryDependencies ++= Seq(
      "com.github.sbt.junit" % "jupiter-interface" % JupiterKeys.jupiterVersion.value % Test
    ),
    fork := true
)