ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "taglessTesting"
  )

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.15",
  "org.typelevel" %% "cats-core" % "2.9.0",
  "org.typelevel" %% "cats-effect" % "3.4.5"
)

