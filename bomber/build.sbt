import Dependencies._

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "ltd.very"
ThisBuild / organizationName := "very ltd."

lazy val root = (project in file("."))
  .settings(
    name := "Bomber"
  )
  
assemblyJarName in assembly := "Bomber.jar"

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
