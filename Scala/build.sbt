import com.typesafe.sbt.packager.Keys.dockerEnvVars
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.Docker

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.5"

val `cats-core-version`      = "2.13.0"
val `cats-effect-version`    = "3.5.7"
val `tapir-version`          = "1.11.13"
val `http4s-version`         = "0.23.30"
val `sttp-client3-version`   = "3.10.2"
val `logback-version`        = "1.5.16"
val `tethys-version`         = "0.29.3"
val `pureconfig-version`     = "0.17.8"
val `asyncapi-circe-version` = "0.11.7"
val `weaver-version`         = "0.8.4"
val `testcontainers-version` = "0.41.8"

val deps = List(
  // cats
  "org.typelevel" %% "cats-core"   % `cats-core-version`,
  "org.typelevel" %% "cats-effect" % `cats-effect-version`,

  // tapir
  "com.softwaremill.sttp.tapir"   %% "tapir-http4s-server"     % `tapir-version`,
  "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui-bundle" % `tapir-version`,
  "com.softwaremill.sttp.tapir"   %% "tapir-json-tethys"       % `tapir-version`,
  "com.softwaremill.sttp.tapir"   %% "tapir-sttp-client"       % `tapir-version`,
  "com.softwaremill.sttp.tapir"   %% "tapir-asyncapi-docs"     % `tapir-version`,
  "com.softwaremill.sttp.apispec" %% "asyncapi-circe-yaml"     % `asyncapi-circe-version`,

  // http4s
  "org.http4s" %% "http4s-ember-server" % `http4s-version`,
  "org.http4s" %% "http4s-dsl"          % `http4s-version`,

  // sttp
  "com.softwaremill.sttp.client3" %% "core" % `sttp-client3-version`,
  "com.softwaremill.sttp.client3" %% "cats" % `sttp-client3-version`,

  // logback
  "ch.qos.logback" % "logback-classic" % `logback-version`,

  // tethys
  "com.tethys-json" %% "tethys-core"       % `tethys-version`,
  "com.tethys-json" %% "tethys-jackson213" % `tethys-version`,
  "com.tethys-json" %% "tethys-derivation" % `tethys-version`,
  "com.tethys-json" %% "tethys-enumeratum" % `tethys-version`,

  // pureconfig
  "com.github.pureconfig" %% "pureconfig-core"           % `pureconfig-version`,
  "com.github.pureconfig" %% "pureconfig-generic-scala3" % `pureconfig-version`
)

val testDeps = List(
  // for tests
  "com.disneystreaming" %% "weaver-cats"                     % `weaver-version`         % Test,
  "com.dimafeng"        %% "testcontainers-scala-scalatest"  % `testcontainers-version` % Test,
  "com.dimafeng"        %% "testcontainers-scala-postgresql" % `testcontainers-version` % Test, // example for psql
  "com.dimafeng" %% "testcontainers-scala-mockserver" % `testcontainers-version` % Test // example for mockserver
)

lazy val `seminar-1` = project
  .settings(
    libraryDependencies ++= deps
  )

lazy val `seminar-2` = project
  .settings(
    libraryDependencies ++= deps
  )

lazy val `seminar-3` = project
  .settings(
    libraryDependencies ++= deps
  )

lazy val `seminar-4` = project
  .settings(
    libraryDependencies ++= deps
  )
  // Set docker build
  .enablePlugins(
    DockerPlugin,
    JavaAppPackaging
  )
  .settings(
    Compile / mainClass  := Some("tbank.ab.Seminar4App"), // class which will be run
    dockerBaseImage      := "eclipse-temurin:21",         // base image for Docker
    dockerExposedPorts   := List(8080),                   // defines exposing ports of the Docker image
    Docker / packageName := "tbank-ab",                   // name of the Docker image
    dockerEnvVars ++= Map("UNUSED_ENV_CONST_VAR" -> "some value") // environment variables for the Docker image
  )

lazy val `seminar-4-it` = project
  .dependsOn(`seminar-4`)
  // Set tests
  .settings(
    libraryDependencies ++= testDeps,
    testFrameworks += new TestFramework("weaver.framework.CatsEffect")
  )
